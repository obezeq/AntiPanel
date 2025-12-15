package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.category.CategoryCreateRequest;
import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.category.CategorySummary;
import com.antipanel.backend.dto.category.CategoryUpdateRequest;
import com.antipanel.backend.entity.Category;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.CategoryMapper;
import com.antipanel.backend.repository.CategoryRepository;
import com.antipanel.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ============ CRUD OPERATIONS ============

    @Override
    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        log.debug("Creating category with name: {}", request.getName());

        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = Category.generateSlug(request.getName());
        }

        // Check slug uniqueness
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Category slug already exists: " + slug);
        }

        Category category = categoryMapper.toEntity(request);
        category.setSlug(slug);

        Category saved = categoryRepository.save(category);
        log.info("Created category with ID: {}", saved.getId());

        return categoryMapper.toResponse(saved);
    }

    @Override
    public CategoryResponse getById(Integer id) {
        log.debug("Getting category by ID: {}", id);
        Category category = findCategoryById(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse getBySlug(String slug) {
        log.debug("Getting category by slug: {}", slug);
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse update(Integer id, CategoryUpdateRequest request) {
        log.debug("Updating category with ID: {}", id);

        Category category = findCategoryById(id);
        categoryMapper.updateEntityFromDto(request, category);

        Category saved = categoryRepository.save(category);
        log.info("Updated category with ID: {}", saved.getId());

        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.debug("Deleting category with ID: {}", id);
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
        log.info("Deleted category with ID: {}", id);
    }

    // ============ LISTING ============

    @Override
    public List<CategoryResponse> getAll() {
        log.debug("Getting all categories sorted");
        List<Category> categories = categoryRepository.findAllCategoriesSorted();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    public List<CategoryResponse> getAllActive() {
        log.debug("Getting all active categories");
        List<Category> categories = categoryRepository.findAllActiveCategories();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    public List<CategoryResponse> getActiveCategoriesWithServiceCount() {
        log.debug("Getting active categories with service count");
        List<Object[]> results = categoryRepository.findActiveCategoriesWithServiceCount();
        return results.stream()
                .map(row -> {
                    Category category = (Category) row[0];
                    Long serviceCount = (Long) row[1];
                    return categoryMapper.toResponseWithCount(category, serviceCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategorySummary> getAllSummaries() {
        log.debug("Getting all category summaries");
        List<Category> categories = categoryRepository.findAllCategoriesSorted();
        return categoryMapper.toSummaryList(categories);
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public CategoryResponse toggleActive(Integer id) {
        log.debug("Toggling active status for category ID: {}", id);
        Category category = findCategoryById(id);
        category.setIsActive(!category.getIsActive());
        Category saved = categoryRepository.save(category);
        log.info("Toggled active status for category ID: {} to {}", id, saved.getIsActive());
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse activate(Integer id) {
        log.debug("Activating category ID: {}", id);
        Category category = findCategoryById(id);
        category.setIsActive(true);
        Category saved = categoryRepository.save(category);
        log.info("Activated category ID: {}", id);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse deactivate(Integer id) {
        log.debug("Deactivating category ID: {}", id);
        Category category = findCategoryById(id);
        category.setIsActive(false);
        Category saved = categoryRepository.save(category);
        log.info("Deactivated category ID: {}", id);
        return categoryMapper.toResponse(saved);
    }

    // ============ STATISTICS ============

    @Override
    public long countActive() {
        log.debug("Counting active categories");
        return categoryRepository.countActiveCategories();
    }

    // ============ VALIDATION ============

    @Override
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsBySlugExcludingCategory(String slug, Integer categoryId) {
        return categoryRepository.findBySlug(slug)
                .map(category -> !category.getId().equals(categoryId))
                .orElse(false);
    }

    // ============ HELPER METHODS ============

    private Category findCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}

package com.antipanel.backend.service;

import com.antipanel.backend.dto.category.CategoryCreateRequest;
import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.category.CategorySummary;
import com.antipanel.backend.dto.category.CategoryUpdateRequest;
import com.antipanel.backend.entity.Category;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.CategoryMapper;
import com.antipanel.backend.repository.CategoryRepository;
import com.antipanel.backend.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryResponse testCategoryResponse;
    private CategoryCreateRequest createRequest;
    private CategoryUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1)
                .name("Instagram")
                .slug("instagram")
                .iconUrl("https://example.com/instagram.png")
                .sortOrder(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        testCategoryResponse = CategoryResponse.builder()
                .id(1)
                .name("Instagram")
                .slug("instagram")
                .iconUrl("https://example.com/instagram.png")
                .sortOrder(0)
                .isActive(true)
                .build();

        createRequest = CategoryCreateRequest.builder()
                .name("Instagram")
                .slug("instagram")
                .iconUrl("https://example.com/instagram.png")
                .sortOrder(0)
                .isActive(true)
                .build();

        updateRequest = CategoryUpdateRequest.builder()
                .name("Instagram Updated")
                .sortOrder(1)
                .build();
    }

    // ============ CREATE TESTS ============

    @Test
    void create_WithSlug_Success() {
        // Given
        when(categoryRepository.existsBySlug("instagram")).thenReturn(false);
        when(categoryMapper.toEntity(any(CategoryCreateRequest.class))).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("instagram");
        verify(categoryRepository).existsBySlug("instagram");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_WithoutSlug_GeneratesSlug() {
        // Given
        CategoryCreateRequest requestWithoutSlug = CategoryCreateRequest.builder()
                .name("TikTok Videos")
                .sortOrder(0)
                .isActive(true)
                .build();

        when(categoryRepository.existsBySlug("tiktok-videos")).thenReturn(false);
        when(categoryMapper.toEntity(any(CategoryCreateRequest.class))).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(testCategoryResponse);

        // When
        categoryService.create(requestWithoutSlug);

        // Then
        verify(categoryRepository).existsBySlug("tiktok-videos");
    }

    @Test
    void create_SlugAlreadyExists_ThrowsConflictException() {
        // Given
        when(categoryRepository.existsBySlug("instagram")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Category slug already exists");
    }

    // ============ GET BY ID TESTS ============

    @Test
    void getById_Success() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.getById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getById_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    // ============ GET BY SLUG TESTS ============

    @Test
    void getBySlug_Success() {
        // Given
        when(categoryRepository.findBySlug("instagram")).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.getBySlug("instagram");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("instagram");
    }

    @Test
    void getBySlug_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(categoryRepository.findBySlug("notfound")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getBySlug("notfound"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    // ============ UPDATE TESTS ============

    @Test
    void update_Success() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(testCategoryResponse);

        // When
        CategoryResponse result = categoryService.update(1, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(categoryMapper).updateEntityFromDto(eq(updateRequest), any(Category.class));
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.update(1, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ DELETE TESTS ============

    @Test
    void delete_Success() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(testCategory);

        // When
        categoryService.delete(1);

        // Then
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.delete(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ============ LISTING TESTS ============

    @Test
    void getAll_Success() {
        // Given
        when(categoryRepository.findAllCategoriesSorted()).thenReturn(List.of(testCategory));
        when(categoryMapper.toResponseList(List.of(testCategory))).thenReturn(List.of(testCategoryResponse));

        // When
        List<CategoryResponse> result = categoryService.getAll();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getAllActive_Success() {
        // Given
        when(categoryRepository.findAllActiveCategories()).thenReturn(List.of(testCategory));
        when(categoryMapper.toResponseList(List.of(testCategory))).thenReturn(List.of(testCategoryResponse));

        // When
        List<CategoryResponse> result = categoryService.getAllActive();

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void getActiveCategoriesWithServiceCount_Success() {
        // Given
        Object[] row = new Object[]{testCategory, 10L};
        List<Object[]> rows = Collections.singletonList(row);
        when(categoryRepository.findActiveCategoriesWithServiceCount()).thenReturn(rows);
        CategoryResponse responseWithCount = CategoryResponse.builder()
                .id(1)
                .name("Instagram")
                .slug("instagram")
                .serviceCount(10L)
                .build();
        when(categoryMapper.toResponseWithCount(testCategory, 10L)).thenReturn(responseWithCount);

        // When
        List<CategoryResponse> result = categoryService.getActiveCategoriesWithServiceCount();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServiceCount()).isEqualTo(10L);
    }

    @Test
    void getAllSummaries_Success() {
        // Given
        CategorySummary summary = CategorySummary.builder()
                .id(1)
                .name("Instagram")
                .slug("instagram")
                .build();
        when(categoryRepository.findAllCategoriesSorted()).thenReturn(List.of(testCategory));
        when(categoryMapper.toSummaryList(List.of(testCategory))).thenReturn(List.of(summary));

        // When
        List<CategorySummary> result = categoryService.getAllSummaries();

        // Then
        assertThat(result).hasSize(1);
    }

    // ============ STATUS OPERATIONS TESTS ============

    @Test
    void toggleActive_FromActiveToInactive() {
        // Given
        testCategory.setIsActive(true);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(
                CategoryResponse.builder().id(1).isActive(false).build()
        );

        // When
        CategoryResponse result = categoryService.toggleActive(1);

        // Then
        verify(categoryRepository).save(argThat(cat -> !cat.getIsActive()));
    }

    @Test
    void toggleActive_FromInactiveToActive() {
        // Given
        testCategory.setIsActive(false);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(
                CategoryResponse.builder().id(1).isActive(true).build()
        );

        // When
        CategoryResponse result = categoryService.toggleActive(1);

        // Then
        verify(categoryRepository).save(argThat(cat -> cat.getIsActive()));
    }

    @Test
    void activate_Success() {
        // Given
        testCategory.setIsActive(false);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(testCategoryResponse);

        // When
        categoryService.activate(1);

        // Then
        verify(categoryRepository).save(argThat(cat -> cat.getIsActive()));
    }

    @Test
    void deactivate_Success() {
        // Given
        testCategory.setIsActive(true);
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(testCategoryResponse);

        // When
        categoryService.deactivate(1);

        // Then
        verify(categoryRepository).save(argThat(cat -> !cat.getIsActive()));
    }

    // ============ STATISTICS TESTS ============

    @Test
    void countActive_Success() {
        // Given
        when(categoryRepository.countActiveCategories()).thenReturn(5L);

        // When
        long count = categoryService.countActive();

        // Then
        assertThat(count).isEqualTo(5L);
    }

    // ============ VALIDATION TESTS ============

    @Test
    void existsBySlug_ReturnsTrue() {
        // Given
        when(categoryRepository.existsBySlug("instagram")).thenReturn(true);

        // When
        boolean exists = categoryService.existsBySlug("instagram");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsBySlugExcludingCategory_SameCategory_ReturnsFalse() {
        // Given
        when(categoryRepository.findBySlug("instagram")).thenReturn(Optional.of(testCategory));

        // When
        boolean exists = categoryService.existsBySlugExcludingCategory("instagram", 1);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsBySlugExcludingCategory_DifferentCategory_ReturnsTrue() {
        // Given
        when(categoryRepository.findBySlug("instagram")).thenReturn(Optional.of(testCategory));

        // When
        boolean exists = categoryService.existsBySlugExcludingCategory("instagram", 2);

        // Then
        assertThat(exists).isTrue();
    }
}

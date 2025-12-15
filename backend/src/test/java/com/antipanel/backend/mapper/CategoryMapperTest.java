package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.category.CategoryCreateRequest;
import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.category.CategorySummary;
import com.antipanel.backend.dto.category.CategoryUpdateRequest;
import com.antipanel.backend.entity.Category;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CategoryMapper.
 */
class CategoryMapperTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        Category category = createTestCategory();

        // When
        CategoryResponse response = mapper.toResponse(category);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(category.getId());
        assertThat(response.getName()).isEqualTo(category.getName());
        assertThat(response.getSlug()).isEqualTo(category.getSlug());
        assertThat(response.getIconUrl()).isEqualTo(category.getIconUrl());
        assertThat(response.getSortOrder()).isEqualTo(category.getSortOrder());
        assertThat(response.getIsActive()).isEqualTo(category.getIsActive());
        assertThat(response.getCreatedAt()).isEqualTo(category.getCreatedAt());
        assertThat(response.getServiceCount()).isNull();  // Not mapped by default
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        // Given
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Instagram")
                .slug("instagram")
                .iconUrl("https://example.com/instagram.png")
                .sortOrder(1)
                .isActive(true)
                .build();

        // When
        Category category = mapper.toEntity(request);

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getId()).isNull();  // ID should be null for new entity
        assertThat(category.getName()).isEqualTo(request.getName());
        assertThat(category.getSlug()).isEqualTo(request.getSlug());
        assertThat(category.getIconUrl()).isEqualTo(request.getIconUrl());
        assertThat(category.getSortOrder()).isEqualTo(request.getSortOrder());
        assertThat(category.getIsActive()).isEqualTo(request.getIsActive());
    }

    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {
        // Given
        Category category = createTestCategory();
        String originalName = category.getName();
        String originalSlug = category.getSlug();

        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
                .iconUrl("https://example.com/new-icon.png")
                .sortOrder(5)
                .build();

        // When
        mapper.updateEntityFromDto(request, category);

        // Then
        assertThat(category.getName()).isEqualTo(originalName);  // Unchanged (null in request)
        assertThat(category.getSlug()).isEqualTo(originalSlug);  // Unchanged (ignored)
        assertThat(category.getIconUrl()).isEqualTo("https://example.com/new-icon.png");
        assertThat(category.getSortOrder()).isEqualTo(5);
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        Category category = createTestCategory();

        // When
        CategorySummary summary = mapper.toSummary(category);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(category.getId());
        assertThat(summary.getName()).isEqualTo(category.getName());
        assertThat(summary.getSlug()).isEqualTo(category.getSlug());
        assertThat(summary.getIconUrl()).isEqualTo(category.getIconUrl());
    }

    @Test
    void toResponseList_ShouldMapAllCategories() {
        // Given
        List<Category> categories = List.of(
                createTestCategory(),
                createTestCategory()
        );
        categories.get(1).setId(2);
        categories.get(1).setName("TikTok");
        categories.get(1).setSlug("tiktok");

        // When
        List<CategoryResponse> responses = mapper.toResponseList(categories);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Instagram");
        assertThat(responses.get(1).getName()).isEqualTo("TikTok");
    }

    @Test
    void toResponseWithCount_ShouldIncludeServiceCount() {
        // Given
        Category category = createTestCategory();
        Long serviceCount = 25L;

        // When
        CategoryResponse response = mapper.toResponseWithCount(category, serviceCount);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(category.getId());
        assertThat(response.getServiceCount()).isEqualTo(25L);
    }

    private Category createTestCategory() {
        Category category = new Category();
        category.setId(1);
        category.setName("Instagram");
        category.setSlug("instagram");
        category.setIconUrl("https://example.com/instagram.png");
        category.setSortOrder(1);
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now().minusDays(30));
        return category;
    }
}

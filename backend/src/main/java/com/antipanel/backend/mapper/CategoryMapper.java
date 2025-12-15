package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.category.CategoryCreateRequest;
import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.category.CategorySummary;
import com.antipanel.backend.dto.category.CategoryUpdateRequest;
import com.antipanel.backend.entity.Category;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Category entity.
 * Handles conversion between Category entity and DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper extends BaseMapper<Category, CategoryResponse, CategoryCreateRequest, CategoryUpdateRequest, CategorySummary> {

    /**
     * Convert Category entity to CategoryResponse DTO.
     * Note: serviceCount is not auto-mapped (must be set manually if needed).
     */
    @Override
    @Mapping(target = "serviceCount", ignore = true)  // Set manually from query result
    CategoryResponse toResponse(Category category);

    /**
     * Convert CategoryCreateRequest to Category entity.
     * Note: Slug may be auto-generated in service if not provided.
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toEntity(CategoryCreateRequest createRequest);

    /**
     * Update Category entity from CategoryUpdateRequest.
     * Null values are ignored (supports partial updates).
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)  // Slug should not be changed after creation
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(CategoryUpdateRequest updateRequest, @MappingTarget Category category);

    /**
     * Convert Category entity to CategorySummary DTO.
     */
    @Override
    CategorySummary toSummary(Category category);

    /**
     * Convert list of Categories to list of CategoryResponse DTOs.
     */
    @Override
    List<CategoryResponse> toResponseList(List<Category> categories);

    /**
     * Convert list of Categories to list of CategorySummary DTOs.
     */
    @Override
    List<CategorySummary> toSummaryList(List<Category> categories);

    /**
     * Convert CategoryResponse to CategoryResponse with serviceCount.
     * Utility method for enriching response with count data.
     */
    default CategoryResponse toResponseWithCount(Category category, Long serviceCount) {
        CategoryResponse response = toResponse(category);
        response.setServiceCount(serviceCount);
        return response;
    }
}

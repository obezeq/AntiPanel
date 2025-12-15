package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.category.CategoryCreateRequest;
import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.category.CategoryUpdateRequest;
import com.antipanel.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin REST Controller for category management.
 * Requires ADMIN role for all operations.
 */
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Categories", description = "Admin category management endpoints")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all categories",
            description = "Returns all categories including inactive, sorted by sort order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.debug("Admin: Getting all categories");
        List<CategoryResponse> response = categoryService.getAll();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get category by ID",
            description = "Returns detailed category information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Getting category by ID: {}", id);
        CategoryResponse response = categoryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new category",
            description = "Creates a new service category")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "409", description = "Category with same slug already exists")
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        log.debug("Admin: Creating new category: {}", request.getName());
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update category",
            description = "Updates category information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        log.debug("Admin: Updating category ID: {}", id);
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete category",
            description = "Deletes a category")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete category with services")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Deleting category ID: {}", id);
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle category active status",
            description = "Enables or disables a category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category status toggled successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<CategoryResponse> toggleActive(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Integer id) {
        log.debug("Admin: Toggling active status for category ID: {}", id);
        CategoryResponse response = categoryService.toggleActive(id);
        return ResponseEntity.ok(response);
    }

}

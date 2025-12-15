package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.category.CategoryCreateRequest;
import com.antipanel.backend.dto.category.CategoryResponse;
import com.antipanel.backend.dto.category.CategoryUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCategoryController Tests")
class AdminCategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private AdminCategoryController adminCategoryController;

    private ObjectMapper objectMapper;
    private CustomUserDetails adminDetails;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminCategoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        User adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .balance(BigDecimal.ZERO)
                .isBanned(false)
                .build();

        adminDetails = new CustomUserDetails(adminUser);

        categoryResponse = CategoryResponse.builder()
                .id(1)
                .name("Social Media")
                .slug("social-media")
                .iconUrl("https://example.com/icon.png")
                .sortOrder(0)
                .isActive(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/categories")
    class GetAllCategories {

        @Test
        @DisplayName("Should return all categories")
        void shouldReturnAllCategories() throws Exception {
            when(categoryService.getAll()).thenReturn(List.of(categoryResponse));

            mockMvc.perform(get("/api/v1/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Social Media"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/categories/{id}")
    class GetCategoryById {

        @Test
        @DisplayName("Should return category by ID")
        void shouldReturnCategoryById() throws Exception {
            when(categoryService.getById(1)).thenReturn(categoryResponse);

            mockMvc.perform(get("/api/v1/admin/categories/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Social Media"));
        }

        @Test
        @DisplayName("Should return 404 when category not found")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            when(categoryService.getById(999))
                    .thenThrow(new ResourceNotFoundException("Category", "id", 999));

            mockMvc.perform(get("/api/v1/admin/categories/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/categories")
    class CreateCategory {

        @Test
        @DisplayName("Should create new category")
        void shouldCreateNewCategory() throws Exception {
            CategoryCreateRequest request = CategoryCreateRequest.builder()
                    .name("Gaming")
                    .slug("gaming")
                    .iconUrl("https://example.com/gaming.png")
                    .sortOrder(1)
                    .build();

            CategoryResponse createdCategory = CategoryResponse.builder()
                    .id(2)
                    .name("Gaming")
                    .slug("gaming")
                    .iconUrl("https://example.com/gaming.png")
                    .sortOrder(1)
                    .isActive(true)
                    .build();

            when(categoryService.create(any(CategoryCreateRequest.class))).thenReturn(createdCategory);

            mockMvc.perform(post("/api/v1/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.name").value("Gaming"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/categories/{id}")
    class UpdateCategory {

        @Test
        @DisplayName("Should update category")
        void shouldUpdateCategory() throws Exception {
            CategoryUpdateRequest request = CategoryUpdateRequest.builder()
                    .name("Updated Social Media")
                    .build();

            CategoryResponse updatedCategory = CategoryResponse.builder()
                    .id(1)
                    .name("Updated Social Media")
                    .slug("social-media")
                    .isActive(true)
                    .build();

            when(categoryService.update(eq(1), any(CategoryUpdateRequest.class))).thenReturn(updatedCategory);

            mockMvc.perform(put("/api/v1/admin/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Social Media"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/categories/{id}")
    class DeleteCategory {

        @Test
        @DisplayName("Should delete category")
        void shouldDeleteCategory() throws Exception {
            doNothing().when(categoryService).delete(1);

            mockMvc.perform(delete("/api/v1/admin/categories/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(categoryService, times(1)).delete(1);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/categories/{id}/toggle-active")
    class ToggleActive {

        @Test
        @DisplayName("Should toggle category active status")
        void shouldToggleCategoryActiveStatus() throws Exception {
            CategoryResponse toggledCategory = CategoryResponse.builder()
                    .id(1)
                    .name("Social Media")
                    .isActive(false)
                    .build();

            when(categoryService.toggleActive(1)).thenReturn(toggledCategory);

            mockMvc.perform(patch("/api/v1/admin/categories/1/toggle-active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }
}

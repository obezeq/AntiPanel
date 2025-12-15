package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.servicetype.ServiceTypeCreateRequest;
import com.antipanel.backend.dto.servicetype.ServiceTypeResponse;
import com.antipanel.backend.dto.servicetype.ServiceTypeUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.ServiceTypeService;
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
@DisplayName("AdminServiceTypeController Tests")
class AdminServiceTypeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServiceTypeService serviceTypeService;

    @InjectMocks
    private AdminServiceTypeController adminServiceTypeController;

    private ObjectMapper objectMapper;
    private CustomUserDetails adminDetails;
    private ServiceTypeResponse serviceTypeResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminServiceTypeController)
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

        serviceTypeResponse = ServiceTypeResponse.builder()
                .id(1)
                .name("Followers")
                .slug("followers")
                .sortOrder(0)
                .isActive(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/service-types/category/{categoryId}")
    class GetServiceTypesByCategory {

        @Test
        @DisplayName("Should return service types by category")
        void shouldReturnServiceTypesByCategory() throws Exception {
            when(serviceTypeService.getAllByCategory(1)).thenReturn(List.of(serviceTypeResponse));

            mockMvc.perform(get("/api/v1/admin/service-types/category/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Followers"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/service-types/{id}")
    class GetServiceTypeById {

        @Test
        @DisplayName("Should return service type by ID")
        void shouldReturnServiceTypeById() throws Exception {
            when(serviceTypeService.getById(1)).thenReturn(serviceTypeResponse);

            mockMvc.perform(get("/api/v1/admin/service-types/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Followers"));
        }

        @Test
        @DisplayName("Should return 404 when service type not found")
        void shouldReturn404WhenServiceTypeNotFound() throws Exception {
            when(serviceTypeService.getById(999))
                    .thenThrow(new ResourceNotFoundException("ServiceType", "id", 999));

            mockMvc.perform(get("/api/v1/admin/service-types/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/service-types")
    class CreateServiceType {

        @Test
        @DisplayName("Should create new service type")
        void shouldCreateNewServiceType() throws Exception {
            ServiceTypeCreateRequest request = ServiceTypeCreateRequest.builder()
                    .name("Likes")
                    .slug("likes")
                    .categoryId(1)
                    .sortOrder(1)
                    .build();

            ServiceTypeResponse createdServiceType = ServiceTypeResponse.builder()
                    .id(2)
                    .name("Likes")
                    .slug("likes")
                    .sortOrder(1)
                    .isActive(true)
                    .build();

            when(serviceTypeService.create(any(ServiceTypeCreateRequest.class))).thenReturn(createdServiceType);

            mockMvc.perform(post("/api/v1/admin/service-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.name").value("Likes"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/service-types/{id}")
    class UpdateServiceType {

        @Test
        @DisplayName("Should update service type")
        void shouldUpdateServiceType() throws Exception {
            ServiceTypeUpdateRequest request = ServiceTypeUpdateRequest.builder()
                    .name("Updated Followers")
                    .build();

            ServiceTypeResponse updatedServiceType = ServiceTypeResponse.builder()
                    .id(1)
                    .name("Updated Followers")
                    .slug("followers")
                    .isActive(true)
                    .build();

            when(serviceTypeService.update(eq(1), any(ServiceTypeUpdateRequest.class))).thenReturn(updatedServiceType);

            mockMvc.perform(put("/api/v1/admin/service-types/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Followers"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/service-types/{id}")
    class DeleteServiceType {

        @Test
        @DisplayName("Should delete service type")
        void shouldDeleteServiceType() throws Exception {
            doNothing().when(serviceTypeService).delete(1);

            mockMvc.perform(delete("/api/v1/admin/service-types/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(serviceTypeService, times(1)).delete(1);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/service-types/{id}/toggle-active")
    class ToggleActive {

        @Test
        @DisplayName("Should toggle service type active status")
        void shouldToggleServiceTypeActiveStatus() throws Exception {
            ServiceTypeResponse toggledServiceType = ServiceTypeResponse.builder()
                    .id(1)
                    .name("Followers")
                    .isActive(false)
                    .build();

            when(serviceTypeService.toggleActive(1)).thenReturn(toggledServiceType);

            mockMvc.perform(patch("/api/v1/admin/service-types/1/toggle-active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }
}

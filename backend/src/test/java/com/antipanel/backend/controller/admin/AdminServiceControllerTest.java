package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.service.ServiceCreateRequest;
import com.antipanel.backend.dto.service.ServiceDetailResponse;
import com.antipanel.backend.dto.service.ServiceResponse;
import com.antipanel.backend.dto.service.ServiceUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.CatalogService;
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
@DisplayName("AdminServiceController Tests")
class AdminServiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CatalogService catalogService;

    @InjectMocks
    private AdminServiceController adminServiceController;

    private ObjectMapper objectMapper;
    private CustomUserDetails adminDetails;
    private ServiceResponse serviceResponse;
    private ServiceDetailResponse serviceDetailResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminServiceController)
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

        serviceResponse = ServiceResponse.builder()
                .id(1)
                .name("Instagram Followers")
                .description("High quality followers")
                .pricePerK(new BigDecimal("5.00"))
                .minQuantity(100)
                .maxQuantity(10000)
                .isActive(true)
                .build();

        serviceDetailResponse = ServiceDetailResponse.builder()
                .id(1)
                .name("Instagram Followers")
                .description("High quality followers")
                .pricePerK(new BigDecimal("5.00"))
                .minQuantity(100)
                .maxQuantity(10000)
                .isActive(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/services")
    class GetAllServices {

        @Test
        @DisplayName("Should return all active catalog services")
        void shouldReturnAllActiveCatalogServices() throws Exception {
            when(catalogService.getActiveCatalogServices()).thenReturn(List.of(serviceResponse));

            mockMvc.perform(get("/api/v1/admin/services")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Instagram Followers"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/services/category/{categoryId}")
    class GetServicesByCategory {

        @Test
        @DisplayName("Should return services by category")
        void shouldReturnServicesByCategory() throws Exception {
            when(catalogService.getActiveCatalogServicesByCategory(1)).thenReturn(List.of(serviceResponse));

            mockMvc.perform(get("/api/v1/admin/services/category/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Instagram Followers"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/services/{id}")
    class GetServiceById {

        @Test
        @DisplayName("Should return service detail by ID")
        void shouldReturnServiceDetailById() throws Exception {
            when(catalogService.getDetailById(1)).thenReturn(serviceDetailResponse);

            mockMvc.perform(get("/api/v1/admin/services/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Instagram Followers"));
        }

        @Test
        @DisplayName("Should return 404 when service not found")
        void shouldReturn404WhenServiceNotFound() throws Exception {
            when(catalogService.getDetailById(999))
                    .thenThrow(new ResourceNotFoundException("Service", "id", 999));

            mockMvc.perform(get("/api/v1/admin/services/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/services")
    class CreateService {

        @Test
        @DisplayName("Should create new service")
        void shouldCreateNewService() throws Exception {
            ServiceCreateRequest request = ServiceCreateRequest.builder()
                    .name("Twitter Likes")
                    .description("Real likes")
                    .categoryId(1)
                    .serviceTypeId(1)
                    .providerServiceId(1)
                    .quality(ServiceQuality.HIGH)
                    .speed(ServiceSpeed.FAST)
                    .pricePerK(new BigDecimal("3.00"))
                    .minQuantity(50)
                    .maxQuantity(5000)
                    .build();

            ServiceResponse createdService = ServiceResponse.builder()
                    .id(2)
                    .name("Twitter Likes")
                    .description("Real likes")
                    .pricePerK(new BigDecimal("3.00"))
                    .isActive(true)
                    .build();

            when(catalogService.create(any(ServiceCreateRequest.class))).thenReturn(createdService);

            mockMvc.perform(post("/api/v1/admin/services")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.name").value("Twitter Likes"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/services/{id}")
    class UpdateService {

        @Test
        @DisplayName("Should update service")
        void shouldUpdateService() throws Exception {
            ServiceUpdateRequest request = ServiceUpdateRequest.builder()
                    .name("Updated Service")
                    .build();

            ServiceResponse updatedService = ServiceResponse.builder()
                    .id(1)
                    .name("Updated Service")
                    .isActive(true)
                    .build();

            when(catalogService.update(eq(1), any(ServiceUpdateRequest.class))).thenReturn(updatedService);

            mockMvc.perform(put("/api/v1/admin/services/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Service"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/services/{id}")
    class DeleteService {

        @Test
        @DisplayName("Should delete service")
        void shouldDeleteService() throws Exception {
            doNothing().when(catalogService).delete(1);

            mockMvc.perform(delete("/api/v1/admin/services/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(catalogService, times(1)).delete(1);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/services/{id}/toggle-active")
    class ToggleActive {

        @Test
        @DisplayName("Should toggle service active status")
        void shouldToggleServiceActiveStatus() throws Exception {
            ServiceResponse toggledService = ServiceResponse.builder()
                    .id(1)
                    .name("Instagram Followers")
                    .isActive(false)
                    .build();

            when(catalogService.toggleActive(1)).thenReturn(toggledService);

            mockMvc.perform(patch("/api/v1/admin/services/1/toggle-active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }
}

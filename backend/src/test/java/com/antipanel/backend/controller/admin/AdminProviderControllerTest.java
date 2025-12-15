package com.antipanel.backend.controller.admin;

import com.antipanel.backend.dto.provider.ProviderCreateRequest;
import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.ProviderUpdateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceCreateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;
import com.antipanel.backend.dto.providerservice.ProviderServiceUpdateRequest;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.UserRole;
import com.antipanel.backend.exception.GlobalExceptionHandler;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.ProviderCatalogService;
import com.antipanel.backend.service.ProviderService;
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
@DisplayName("AdminProviderController Tests")
class AdminProviderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProviderService providerService;

    @Mock
    private ProviderCatalogService providerCatalogService;

    @InjectMocks
    private AdminProviderController adminProviderController;

    private ObjectMapper objectMapper;
    private CustomUserDetails adminDetails;
    private ProviderResponse providerResponse;
    private ProviderServiceResponse providerServiceResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminProviderController)
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

        providerResponse = ProviderResponse.builder()
                .id(1)
                .name("SMM Provider")
                .apiUrl("https://api.provider.com")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        providerServiceResponse = ProviderServiceResponse.builder()
                .id(1)
                .providerServiceId("100")
                .name("Instagram Followers")
                .costPerK(new BigDecimal("2.00"))
                .minQuantity(100)
                .maxQuantity(100000)
                .isActive(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/providers")
    class GetAllProviders {

        @Test
        @DisplayName("Should return all providers")
        void shouldReturnAllProviders() throws Exception {
            when(providerService.getAll()).thenReturn(List.of(providerResponse));

            mockMvc.perform(get("/api/v1/admin/providers")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("SMM Provider"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/providers/active")
    class GetActiveProviders {

        @Test
        @DisplayName("Should return all active providers")
        void shouldReturnAllActiveProviders() throws Exception {
            when(providerService.getAllActive()).thenReturn(List.of(providerResponse));

            mockMvc.perform(get("/api/v1/admin/providers/active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].isActive").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/providers/{id}")
    class GetProviderById {

        @Test
        @DisplayName("Should return provider by ID")
        void shouldReturnProviderById() throws Exception {
            when(providerService.getById(1)).thenReturn(providerResponse);

            mockMvc.perform(get("/api/v1/admin/providers/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("SMM Provider"));
        }

        @Test
        @DisplayName("Should return 404 when provider not found")
        void shouldReturn404WhenProviderNotFound() throws Exception {
            when(providerService.getById(999))
                    .thenThrow(new ResourceNotFoundException("Provider", "id", 999));

            mockMvc.perform(get("/api/v1/admin/providers/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/providers")
    class CreateProvider {

        @Test
        @DisplayName("Should create new provider")
        void shouldCreateNewProvider() throws Exception {
            ProviderCreateRequest request = ProviderCreateRequest.builder()
                    .name("New Provider")
                    .apiUrl("https://api.newprovider.com")
                    .apiKey("secret-key")
                    .build();

            ProviderResponse createdProvider = ProviderResponse.builder()
                    .id(2)
                    .name("New Provider")
                    .apiUrl("https://api.newprovider.com")
                    .balance(BigDecimal.ZERO)
                    .isActive(true)
                    .build();

            when(providerService.create(any(ProviderCreateRequest.class))).thenReturn(createdProvider);

            mockMvc.perform(post("/api/v1/admin/providers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.name").value("New Provider"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/providers/{id}")
    class UpdateProvider {

        @Test
        @DisplayName("Should update provider")
        void shouldUpdateProvider() throws Exception {
            ProviderUpdateRequest request = ProviderUpdateRequest.builder()
                    .name("Updated Provider")
                    .build();

            ProviderResponse updatedProvider = ProviderResponse.builder()
                    .id(1)
                    .name("Updated Provider")
                    .isActive(true)
                    .build();

            when(providerService.update(eq(1), any(ProviderUpdateRequest.class))).thenReturn(updatedProvider);

            mockMvc.perform(put("/api/v1/admin/providers/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Provider"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/providers/{id}")
    class DeleteProvider {

        @Test
        @DisplayName("Should delete provider")
        void shouldDeleteProvider() throws Exception {
            doNothing().when(providerService).delete(1);

            mockMvc.perform(delete("/api/v1/admin/providers/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(providerService, times(1)).delete(1);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/providers/{id}/toggle-active")
    class ToggleProviderActive {

        @Test
        @DisplayName("Should toggle provider active status")
        void shouldToggleProviderActiveStatus() throws Exception {
            ProviderResponse toggledProvider = ProviderResponse.builder()
                    .id(1)
                    .name("SMM Provider")
                    .isActive(false)
                    .build();

            when(providerService.toggleActive(1)).thenReturn(toggledProvider);

            mockMvc.perform(patch("/api/v1/admin/providers/1/toggle-active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }

    // Provider Service Tests

    @Nested
    @DisplayName("GET /api/v1/admin/providers/{providerId}/services")
    class GetProviderServices {

        @Test
        @DisplayName("Should return provider's services")
        void shouldReturnProviderServices() throws Exception {
            when(providerCatalogService.getAllByProvider(1)).thenReturn(List.of(providerServiceResponse));

            mockMvc.perform(get("/api/v1/admin/providers/1/services")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("Instagram Followers"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/providers/services/{id}")
    class GetProviderServiceById {

        @Test
        @DisplayName("Should return provider service by ID")
        void shouldReturnProviderServiceById() throws Exception {
            when(providerCatalogService.getById(1)).thenReturn(providerServiceResponse);

            mockMvc.perform(get("/api/v1/admin/providers/services/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Instagram Followers"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/providers/services")
    class CreateProviderService {

        @Test
        @DisplayName("Should create new provider service")
        void shouldCreateNewProviderService() throws Exception {
            ProviderServiceCreateRequest request = ProviderServiceCreateRequest.builder()
                    .providerId(1)
                    .providerServiceId("200")
                    .name("Twitter Followers")
                    .costPerK(new BigDecimal("3.00"))
                    .minQuantity(100)
                    .maxQuantity(50000)
                    .build();

            ProviderServiceResponse createdService = ProviderServiceResponse.builder()
                    .id(2)
                    .providerServiceId("200")
                    .name("Twitter Followers")
                    .costPerK(new BigDecimal("3.00"))
                    .isActive(true)
                    .build();

            when(providerCatalogService.create(any(ProviderServiceCreateRequest.class))).thenReturn(createdService);

            mockMvc.perform(post("/api/v1/admin/providers/services")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.name").value("Twitter Followers"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/providers/services/{id}")
    class UpdateProviderService {

        @Test
        @DisplayName("Should update provider service")
        void shouldUpdateProviderService() throws Exception {
            ProviderServiceUpdateRequest request = ProviderServiceUpdateRequest.builder()
                    .name("Updated Service")
                    .build();

            ProviderServiceResponse updatedService = ProviderServiceResponse.builder()
                    .id(1)
                    .name("Updated Service")
                    .isActive(true)
                    .build();

            when(providerCatalogService.update(eq(1), any(ProviderServiceUpdateRequest.class)))
                    .thenReturn(updatedService);

            mockMvc.perform(put("/api/v1/admin/providers/services/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Service"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/providers/services/{id}")
    class DeleteProviderService {

        @Test
        @DisplayName("Should delete provider service")
        void shouldDeleteProviderService() throws Exception {
            doNothing().when(providerCatalogService).delete(1);

            mockMvc.perform(delete("/api/v1/admin/providers/services/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(providerCatalogService, times(1)).delete(1);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/providers/services/{id}/toggle-active")
    class ToggleProviderServiceActive {

        @Test
        @DisplayName("Should toggle provider service active status")
        void shouldToggleProviderServiceActiveStatus() throws Exception {
            ProviderServiceResponse toggledService = ProviderServiceResponse.builder()
                    .id(1)
                    .name("Instagram Followers")
                    .isActive(false)
                    .build();

            when(providerCatalogService.toggleActive(1)).thenReturn(toggledService);

            mockMvc.perform(patch("/api/v1/admin/providers/services/1/toggle-active")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }
}

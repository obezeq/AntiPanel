package com.antipanel.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for CORS configuration.
 * Verifies that CORS headers are properly returned for preflight and actual requests.
 * Uses WebTestClient for real HTTP testing of CORS at the filter level.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("CORS Configuration Tests")
class CorsConfigTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    private static final String ALLOWED_ORIGIN = "http://localhost:4200";
    private static final String DISALLOWED_ORIGIN = "http://malicious-site.com";

    @Nested
    @DisplayName("OPTIONS Preflight Requests")
    class PreflightRequests {

        @Test
        @DisplayName("Should return CORS headers on OPTIONS preflight from allowed origin")
        void shouldReturnCorsHeadersOnPreflightFromAllowedOrigin() {
            webTestClient.options()
                    .uri("/api/v1/auth/login")
                    .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                    .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN)
                    .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)
                    .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        @Test
        @DisplayName("Should return CORS headers for register endpoint preflight")
        void shouldReturnCorsHeadersForRegisterPreflight() {
            webTestClient.options()
                    .uri("/api/v1/auth/register")
                    .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                    .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN);
        }

        @Test
        @DisplayName("Should reject preflight from disallowed origin")
        void shouldRejectPreflightFromDisallowedOrigin() {
            webTestClient.options()
                    .uri("/api/v1/auth/login")
                    .header(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN)
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    @Nested
    @DisplayName("Actual Requests with CORS")
    class ActualRequests {

        @Test
        @DisplayName("Should include CORS headers on actual POST request from allowed origin")
        void shouldIncludeCorsHeadersOnActualRequest() {
            webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"email\":\"test@test.com\",\"password\":\"test123\"}")
                    .exchange()
                    .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
                    .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN);
        }

        @Test
        @DisplayName("Should include Vary header for proper caching")
        void shouldIncludeVaryHeader() {
            webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"email\":\"test@test.com\",\"password\":\"test123\"}")
                    .exchange()
                    .expectHeader().exists(HttpHeaders.VARY);
        }
    }

    @Nested
    @DisplayName("CORS for Protected Endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("Should allow CORS preflight for protected endpoints")
        void shouldAllowCorsPreflightForProtectedEndpoints() {
            webTestClient.options()
                    .uri("/api/v1/users/me")
                    .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        }
    }
}

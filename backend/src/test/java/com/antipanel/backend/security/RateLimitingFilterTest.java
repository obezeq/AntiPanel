package com.antipanel.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitingFilter Tests")
class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRemoteAddr("192.168.1.100");
        request.setServletPath("/api/v1/users");
    }

    @Nested
    @DisplayName("Rate Limiting Behavior")
    class RateLimitingBehavior {

        @Test
        @DisplayName("Should allow requests under the rate limit")
        void shouldAllowRequestsUnderRateLimit() throws ServletException, IOException {
            // Execute a single request
            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Verify request was allowed
            verify(filterChain, times(1)).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("Should allow multiple requests under the rate limit")
        void shouldAllowMultipleRequestsUnderRateLimit() throws ServletException, IOException {
            // Execute 50 requests (well under the 100/minute limit)
            for (int i = 0; i < 50; i++) {
                MockHttpServletResponse newResponse = new MockHttpServletResponse();
                rateLimitingFilter.doFilterInternal(request, newResponse, filterChain);
                assertThat(newResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
            }

            // Verify all requests were allowed
            verify(filterChain, times(50)).doFilter(eq(request), any());
        }

        @Test
        @DisplayName("Should block requests when rate limit is exceeded")
        void shouldBlockRequestsWhenRateLimitExceeded() throws ServletException, IOException {
            // Execute 100 requests to exhaust the bucket
            for (int i = 0; i < 100; i++) {
                MockHttpServletResponse newResponse = new MockHttpServletResponse();
                rateLimitingFilter.doFilterInternal(request, newResponse, filterChain);
            }

            // The 101st request should be blocked
            MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, blockedResponse, filterChain);

            assertThat(blockedResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
            assertThat(blockedResponse.getContentType()).isEqualTo("application/json");
            assertThat(blockedResponse.getContentAsString()).contains("Too Many Requests");
        }

        @Test
        @DisplayName("Should track rate limits per IP address")
        void shouldTrackRateLimitsPerIpAddress() throws ServletException, IOException {
            // Request from first IP
            request.setRemoteAddr("192.168.1.1");
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
            verify(filterChain, times(1)).doFilter(eq(request), any());

            // Request from second IP (different bucket)
            MockHttpServletRequest request2 = new MockHttpServletRequest();
            request2.setRemoteAddr("192.168.1.2");
            request2.setServletPath("/api/v1/users");
            MockHttpServletResponse response2 = new MockHttpServletResponse();

            rateLimitingFilter.doFilterInternal(request2, response2, filterChain);
            verify(filterChain, times(1)).doFilter(eq(request2), any());
        }

        @Test
        @DisplayName("Should extract client IP from X-Forwarded-For header")
        void shouldExtractClientIpFromXForwardedForHeader() throws ServletException, IOException {
            request.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1, 172.16.0.1");

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            // Should use the first IP in the X-Forwarded-For chain
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Path Exclusion")
    class PathExclusion {

        @Test
        @DisplayName("Should not filter actuator endpoints")
        void shouldNotFilterActuatorEndpoints() {
            request.setServletPath("/actuator/health");
            assertThat(rateLimitingFilter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("Should not filter swagger-ui endpoints")
        void shouldNotFilterSwaggerUiEndpoints() {
            request.setServletPath("/swagger-ui/index.html");
            assertThat(rateLimitingFilter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("Should not filter api-docs endpoints")
        void shouldNotFilterApiDocsEndpoints() {
            request.setServletPath("/v3/api-docs");
            assertThat(rateLimitingFilter.shouldNotFilter(request)).isTrue();
        }

        @Test
        @DisplayName("Should filter regular API endpoints")
        void shouldFilterRegularApiEndpoints() {
            request.setServletPath("/api/v1/users");
            assertThat(rateLimitingFilter.shouldNotFilter(request)).isFalse();
        }

        @Test
        @DisplayName("Should filter admin endpoints")
        void shouldFilterAdminEndpoints() {
            request.setServletPath("/api/v1/admin/users");
            assertThat(rateLimitingFilter.shouldNotFilter(request)).isFalse();
        }
    }

    @Nested
    @DisplayName("Response Format")
    class ResponseFormat {

        @Test
        @DisplayName("Should return proper JSON error response when rate limited")
        void shouldReturnProperJsonErrorResponseWhenRateLimited() throws ServletException, IOException {
            // Exhaust the rate limit
            for (int i = 0; i < 100; i++) {
                MockHttpServletResponse newResponse = new MockHttpServletResponse();
                rateLimitingFilter.doFilterInternal(request, newResponse, filterChain);
            }

            // Make one more request that should be rate limited
            MockHttpServletResponse rateLimitedResponse = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, rateLimitedResponse, filterChain);

            assertThat(rateLimitedResponse.getStatus()).isEqualTo(429);
            assertThat(rateLimitedResponse.getContentType()).isEqualTo("application/json");
            String responseBody = rateLimitedResponse.getContentAsString();
            assertThat(responseBody).contains("\"error\"");
            assertThat(responseBody).contains("\"message\"");
            assertThat(responseBody).contains("\"status\"");
            assertThat(responseBody).contains("429");
        }
    }
}

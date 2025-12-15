package com.antipanel.backend.security.jwt;

import com.antipanel.backend.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    // Base64-encoded 256-bit (32-byte) key for testing - must be at least 32 bytes for HS256
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "this_is_a_very_long_test_secret_key_for_jwt_authentication_256_bits".getBytes()
    );

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties(
                TEST_SECRET,
                900000L,  // 15 minutes
                604800000L,  // 7 days
                "test-issuer"
        );
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Nested
    @DisplayName("Token Generation")
    class TokenGeneration {

        @Test
        @DisplayName("Should generate access token from authentication")
        void shouldGenerateAccessTokenFromAuthentication() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "test@example.com",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

            String token = jwtTokenProvider.generateAccessToken(authentication);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("Should generate access token from username and authorities")
        void shouldGenerateAccessTokenFromUsernameAndAuthorities() {
            String token = jwtTokenProvider.generateAccessToken("test@example.com", "ROLE_USER,ROLE_ADMIN");

            assertThat(token).isNotNull();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
            assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("test@example.com");
            assertThat(jwtTokenProvider.getAuthoritiesFromToken(token)).isEqualTo("ROLE_USER,ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should generate refresh token")
        void shouldGenerateRefreshToken() {
            String token = jwtTokenProvider.generateRefreshToken("test@example.com");

            assertThat(token).isNotNull();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
            assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
            assertThat(jwtTokenProvider.isAccessToken(token)).isFalse();
        }

        @Test
        @DisplayName("Should set correct token type for access token")
        void shouldSetCorrectTokenTypeForAccessToken() {
            String token = jwtTokenProvider.generateAccessToken("test@example.com", "ROLE_USER");

            assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("access");
            assertThat(jwtTokenProvider.isAccessToken(token)).isTrue();
            assertThat(jwtTokenProvider.isRefreshToken(token)).isFalse();
        }

        @Test
        @DisplayName("Should set correct token type for refresh token")
        void shouldSetCorrectTokenTypeForRefreshToken() {
            String token = jwtTokenProvider.generateRefreshToken("test@example.com");

            assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("refresh");
            assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
            assertThat(jwtTokenProvider.isAccessToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("Should validate valid token")
        void shouldValidateValidToken() {
            String token = jwtTokenProvider.generateAccessToken("test@example.com", "ROLE_USER");

            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid token")
        void shouldRejectInvalidToken() {
            assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("Should reject empty token")
        void shouldRejectEmptyToken() {
            assertThat(jwtTokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("Should reject expired token")
        void shouldRejectExpiredToken() {
            // Create properties with very short expiration
            JwtProperties shortExpirationProps = new JwtProperties(
                    TEST_SECRET,
                    1L,  // 1 millisecond
                    1L,
                    "test-issuer"
            );
            JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(shortExpirationProps);
            String token = shortExpirationProvider.generateAccessToken("test@example.com", "ROLE_USER");

            // Wait for token to expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThat(shortExpirationProvider.validateToken(token)).isFalse();
            assertThat(shortExpirationProvider.isTokenExpired(token)).isTrue();
        }
    }

    @Nested
    @DisplayName("Token Claims Extraction")
    class TokenClaimsExtraction {

        @Test
        @DisplayName("Should extract username from token")
        void shouldExtractUsernameFromToken() {
            String token = jwtTokenProvider.generateAccessToken("user@example.com", "ROLE_USER");

            assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("Should extract authorities from token")
        void shouldExtractAuthoritiesFromToken() {
            String token = jwtTokenProvider.generateAccessToken("test@example.com", "ROLE_USER,ROLE_ADMIN");

            assertThat(jwtTokenProvider.getAuthoritiesFromToken(token)).isEqualTo("ROLE_USER,ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should extract expiration from token")
        void shouldExtractExpirationFromToken() {
            String token = jwtTokenProvider.generateAccessToken("test@example.com", "ROLE_USER");

            assertThat(jwtTokenProvider.getExpirationFromToken(token)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Configuration Properties")
    class ConfigurationProperties {

        @Test
        @DisplayName("Should return access token expiration")
        void shouldReturnAccessTokenExpiration() {
            assertThat(jwtTokenProvider.getAccessTokenExpiration()).isEqualTo(900000L);
        }

        @Test
        @DisplayName("Should return refresh token expiration")
        void shouldReturnRefreshTokenExpiration() {
            assertThat(jwtTokenProvider.getRefreshTokenExpiration()).isEqualTo(604800000L);
        }
    }
}

package com.antipanel.backend.security.jwt;

import com.antipanel.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for creating and validating JWT tokens.
 * Uses HS256 algorithm with a Base64-encoded secret key.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token from authentication object.
     *
     * @param authentication Spring Security authentication
     * @return JWT access token
     */
    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return generateAccessToken(username, authorities);
    }

    /**
     * Generate access token from username and authorities.
     *
     * @param username    User email
     * @param authorities Comma-separated authorities
     * @return JWT access token
     */
    public String generateAccessToken(String username, String authorities) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("authorities", authorities)
                .claim("type", "access")
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate refresh token from username.
     *
     * @param username User email
     * @return JWT refresh token
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extract username (subject) from token.
     *
     * @param token JWT token
     * @return Username/email
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract authorities from token.
     *
     * @param token JWT token
     * @return Comma-separated authorities
     */
    public String getAuthoritiesFromToken(String token) {
        return getClaims(token).get("authorities", String.class);
    }

    /**
     * Get token type (access or refresh).
     *
     * @param token JWT token
     * @return Token type
     */
    public String getTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    /**
     * Validate token signature and expiration.
     *
     * @param token JWT token
     * @return true if valid
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT token is empty or null: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is an access token.
     *
     * @param token JWT token
     * @return true if access token
     */
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if token is a refresh token.
     *
     * @param token JWT token
     * @return true if refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get token expiration date.
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Check if token is expired.
     *
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Parse claims from token.
     *
     * @param token JWT token
     * @return Claims object
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get access token expiration in milliseconds.
     *
     * @return Expiration time in ms
     */
    public long getAccessTokenExpiration() {
        return jwtProperties.accessTokenExpiration();
    }

    /**
     * Get refresh token expiration in milliseconds.
     *
     * @return Expiration time in ms
     */
    public long getRefreshTokenExpiration() {
        return jwtProperties.refreshTokenExpiration();
    }
}

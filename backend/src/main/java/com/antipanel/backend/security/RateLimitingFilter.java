package com.antipanel.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j token bucket algorithm.
 * Limits requests per IP address to prevent abuse.
 *
 * <p>Configuration: 100 requests per minute per IP address.</p>
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    /**
     * Cache of rate limit buckets per client IP address.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Maximum requests allowed per minute per IP.
     */
    private static final int REQUESTS_PER_MINUTE = 100;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::createBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            sendRateLimitExceededResponse(response);
        }
    }

    /**
     * Creates a new rate limit bucket for a client.
     *
     * @param key The client identifier (IP address)
     * @return A new Bucket with the configured rate limit
     */
    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REQUESTS_PER_MINUTE)
                .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Extracts the client IP address from the request.
     * Handles X-Forwarded-For header for proxied requests.
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Sends HTTP 429 Too Many Requests response.
     *
     * @param response HTTP response
     * @throws IOException if writing to response fails
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"error": "Too Many Requests", "message": "Rate limit exceeded. Please try again later.", "status": 429}
                """);
    }

    /**
     * Determines which requests should bypass rate limiting.
     * Excludes CORS preflight (OPTIONS), actuator, swagger, and API docs endpoints.
     *
     * @param request HTTP request
     * @return true if the request should not be rate limited
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Skip CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        return path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}

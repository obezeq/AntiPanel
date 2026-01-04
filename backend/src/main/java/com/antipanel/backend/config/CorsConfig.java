package com.antipanel.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS configuration for the AntiPanel API.
 *
 * <p>This configuration creates a CorsFilter with HIGHEST_PRECEDENCE to ensure
 * CORS headers are processed before Spring Security filters. This is critical
 * because preflight OPTIONS requests don't contain authentication cookies.</p>
 *
 * <p>Configuration is externalized via {@link CorsProperties} allowing different
 * origins per environment (dev, docker, prod) through YAML and environment variables.</p>
 *
 * @see CorsProperties
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html">Spring Security CORS</a>
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
@Slf4j
public class CorsConfig {

    private final CorsProperties corsProperties;

    /**
     * Creates a CorsFilter bean with highest precedence.
     *
     * <p>The HIGHEST_PRECEDENCE order ensures this filter runs before
     * Spring Security's filter chain, allowing preflight requests to
     * receive proper CORS headers without authentication.</p>
     *
     * @return CorsFilter configured with properties from application YAML
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        log.info("Configuring CORS with allowed origins: {}", corsProperties.getAllowedOrigins());

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        config.setAllowedHeaders(corsProperties.getAllowedHeaders());
        config.setExposedHeaders(corsProperties.getExposedHeaders());
        config.setAllowCredentials(corsProperties.isAllowCredentials());
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

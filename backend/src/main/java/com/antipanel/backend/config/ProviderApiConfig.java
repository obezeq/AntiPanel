package com.antipanel.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuration for external provider API clients.
 * Provides a configured RestClient bean with timeouts and retry settings.
 */
@Configuration
public class ProviderApiConfig {

    /**
     * Default connection timeout in milliseconds
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;

    /**
     * Default read timeout in milliseconds
     */
    private static final int DEFAULT_READ_TIMEOUT = 30000;

    /**
     * Creates a RestClient.Builder bean for provider API calls.
     * Uses SimpleClientHttpRequestFactory for JDK HttpClient.
     */
    @Bean
    public RestClient.Builder providerRestClientBuilder() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(DEFAULT_CONNECT_TIMEOUT));
        factory.setReadTimeout(Duration.ofMillis(DEFAULT_READ_TIMEOUT));

        return RestClient.builder()
                .requestFactory(factory);
    }

    /**
     * Configuration properties for provider API settings.
     */
    @ConfigurationProperties(prefix = "app.providers")
    public record ProviderProperties(
            int connectTimeout,
            int readTimeout,
            int retryAttempts,
            int retryDelay
    ) {
        public ProviderProperties {
            if (connectTimeout <= 0) connectTimeout = DEFAULT_CONNECT_TIMEOUT;
            if (readTimeout <= 0) readTimeout = DEFAULT_READ_TIMEOUT;
            if (retryAttempts <= 0) retryAttempts = 3;
            if (retryDelay <= 0) retryDelay = 1000;
        }
    }
}

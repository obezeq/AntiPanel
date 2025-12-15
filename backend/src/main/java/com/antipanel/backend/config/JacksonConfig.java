package com.antipanel.backend.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson ObjectMapper configuration for Spring Boot 4.
 * Configures JSON serialization/deserialization settings.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 Date/Time support
        objectMapper.registerModule(new JavaTimeModule());

        // Prevent timestamps, use ISO-8601 format
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignore unknown properties during deserialization
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Don't fail on empty beans
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }
}

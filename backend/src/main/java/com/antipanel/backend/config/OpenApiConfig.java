package com.antipanel.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for AntiPanel API documentation.
 * Access Swagger UI at: /swagger-ui.html
 * Access API docs at: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    private Info apiInfo() {
        return new Info()
                .title("AntiPanel API")
                .description("SMM Panel Backend API for social media marketing services")
                .version("1.0.0")
                .contact(new Contact()
                        .name("AntiPanel Team")
                        .email("support@antipanel.com"))
                .license(new License()
                        .name("Private")
                        .url("https://antipanel.com"));
    }

    private List<Server> servers() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Development Server");

        Server prodServer = new Server()
                .url("https://api.antipanel.com")
                .description("Production Server");

        return List.of(devServer, prodServer);
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token"));
    }
}

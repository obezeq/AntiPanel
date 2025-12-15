package com.antipanel.backend;

import com.antipanel.backend.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test that verifies the Spring Boot application context loads successfully.
 * Uses Testcontainers to provide a PostgreSQL database for the full context test.
 */
@SpringBootTest
@Testcontainers
@Import(TestSecurityConfig.class)
class AntiPanelBackendApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

	@Test
	void contextLoads() {
	}

}

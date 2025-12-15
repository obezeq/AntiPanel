package com.antipanel.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AntiPanelBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AntiPanelBackendApplication.class, args);
	}

}

package com.example.ImageHandling.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Milad Mofidi (milad.mofidi@cmas-systems.com)
 * 9/12/2024
 */
@Configuration
public class KeycloakConfig {
	@Bean
	public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}
}

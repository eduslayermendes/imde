package com.example.ImageHandling.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.mappings}")
    private String corsMappings;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.exposed-headers}")
    private String[] exposedHeaders;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(corsMappings)
            .allowedOrigins(allowedOrigins)
            .allowedMethods(allowedMethods)
            .allowCredentials(allowCredentials)
            .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept")
            .exposedHeaders(exposedHeaders);

    }
}

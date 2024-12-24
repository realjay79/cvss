package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://cvss31lookupservice-6053310091.us-central1.run.app") // Front-end origin
                .allowedMethods("POST", "GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

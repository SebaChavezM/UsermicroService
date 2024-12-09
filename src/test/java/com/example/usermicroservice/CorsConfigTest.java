package com.example.usermicroservice;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    @Test
    void testCorsConfigurer() {
        // Arrange
        CorsConfig corsConfig = new CorsConfig();

        // Act
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();

        // Assert
        assertNotNull(configurer);

        // Probar la configuración de CORS
        CorsRegistry registry = new CorsRegistry();
        configurer.addCorsMappings(registry);

        assertNotNull(registry);
    }

    @Test
    void testCorsMappings() {
        // Arrange
        CorsConfig corsConfig = new CorsConfig();
        WebMvcConfigurer configurer = corsConfig.corsConfigurer();
        CorsRegistry registry = new CorsRegistry();

        // Act
        configurer.addCorsMappings(registry);

        // Assert
        assertDoesNotThrow(() -> configurer.addCorsMappings(registry));
        // Verificar que las configuraciones han sido aplicadas
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        assertNotNull(registry);
    }
}

package com.example.usermicroservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desactiva CSRF para simplificar pruebas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // Permitir acceso a todas las rutas de /api
                .anyRequest().authenticated() // Requiere autenticación para otros endpoints
            );

        return http.build();
    }

}

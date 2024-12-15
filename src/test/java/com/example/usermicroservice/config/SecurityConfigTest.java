package com.example.usermicroservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void testSecurityFilterChainBeanExists() {
        // Assert that the SecurityFilterChain bean is correctly created
        assertNotNull(securityFilterChain, "SecurityFilterChain bean should not be null");
    }
}

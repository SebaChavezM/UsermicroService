package com.example.usermicroservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UsermicroserviceApplicationTests {

    @Test
    void testMainMethod() {
        // Arrange
        String[] args = new String[]{"--spring.main.banner-mode=off"};

        // Act & Assert
        // Verifica que no se lance ninguna excepción al ejecutar el método main
        assertDoesNotThrow(() -> UsermicroserviceApplication.main(args));
    }
}

package com.example.usermicroservice.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testUserGettersAndSetters() {
        // Arrange
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        // Act
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("securePassword123");
        user.setRole("ADMIN");
        user.setCreatedAt(now);
        user.setDireccion("123 Main St");
        user.setTelefono("123456789");

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("securePassword123", user.getPassword());
        assertEquals("ADMIN", user.getRole());
        assertEquals(now, user.getCreatedAt());
        assertEquals("123 Main St", user.getDireccion());
        assertEquals("123456789", user.getTelefono());
    }

    @Test
    void testValidation_ValidUser() {
        // Arrange
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("securePassword123");
        user.setRole("USER");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertTrue(violations.isEmpty(), "There should be no validation errors");
    }

    @Test
    void testValidation_InvalidUser() {
        // Arrange
        User user = new User();
        user.setName(""); // Invalid: Blank name
        user.setEmail("invalid-email"); // Invalid: Not a valid email
        user.setPassword(""); // Invalid: Blank password
        user.setRole(""); // Invalid: Blank role

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertFalse(violations.isEmpty(), "There should be validation errors");
        assertEquals(4, violations.size()); // Expect 4 validation errors
    }

    @Test
    void testValidation_NullFields() {
        // Arrange
        User user = new User();
        user.setName(null); // Null name
        user.setEmail(null); // Null email
        user.setPassword(null); // Null password
        user.setRole(null); // Null role

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Assert
        assertFalse(violations.isEmpty(), "There should be validation errors for null fields");
        assertEquals(4, violations.size()); // Expect 4 validation errors for null fields
    }
}

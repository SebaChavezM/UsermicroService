package com.example.usermicroservice.controller;

import com.example.usermicroservice.entity.User;
import com.example.usermicroservice.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
        session = mock(HttpSession.class);
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(new User(), new User());
        when(userService.getAllUsers()).thenReturn(users);

        // Act
        List<User> result = userController.getAllUsers();

        // Assert
        assertEquals(users, result);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUserByIdFound() {
        // Arrange
        User user = new User();
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<User> response = userController.getUserById(1L);

        // Assert
        assertEquals(ResponseEntity.ok(user), response);
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void testGetUserByIdNotFound() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<User> response = userController.getUserById(1L);

        // Assert
        assertEquals(ResponseEntity.notFound().build(), response);
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void testCreateUser() {
        // Arrange
        User user = new User();
        when(userService.createUser(user)).thenReturn(user);

        // Act
        ResponseEntity<User> response = userController.createUser(user);

        // Assert
        assertEquals(ResponseEntity.ok(user), response);
        verify(userService, times(1)).createUser(user);
    }

    @Test
    void testUpdateUserSuccess() {
        // Arrange
        User user = new User();
        when(userService.updateUser(1L, user)).thenReturn(user);

        // Act
        ResponseEntity<User> response = userController.updateUser(1L, user);

        // Assert
        assertEquals(ResponseEntity.ok(user), response);
        verify(userService, times(1)).updateUser(1L, user);
    }

    @Test
    void testUpdateUserNotFound() {
        // Arrange
        User user = new User();
        when(userService.updateUser(1L, user)).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<User> response = userController.updateUser(1L, user);

        // Assert
        assertEquals(ResponseEntity.notFound().build(), response);
        verify(userService, times(1)).updateUser(1L, user);
    }

    @Test
    void testGetAuthenticatedUserAuthenticated() {
        // Arrange
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        // Act
        ResponseEntity<User> response = userController.getAuthenticatedUser(session);

        // Assert
        assertEquals(ResponseEntity.ok(user), response);
    }

    @Test
    void testGetAuthenticatedUserNotAuthenticated() {
        // Arrange
        when(session.getAttribute("user")).thenReturn(null);

        // Act
        ResponseEntity<User> response = userController.getAuthenticatedUser(session);

        // Assert
        assertEquals(ResponseEntity.status(401).build(), response);
    }

    @Test
    void testDeleteUserAsAdmin() {
        // Arrange
        User adminUser = new User();
        adminUser.setRole("ADMIN");
        when(session.getAttribute("user")).thenReturn(adminUser);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L, session);

        // Assert
        assertEquals(ResponseEntity.noContent().build(), response);
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void testDeleteUserNotAdmin() {
        // Arrange
        User regularUser = new User();
        regularUser.setRole("USER");
        when(session.getAttribute("user")).thenReturn(regularUser);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L, session);

        // Assert
        assertEquals(ResponseEntity.status(403).build(), response);
        verify(userService, never()).deleteUser(1L);
    }

    @Test
    void testDeleteUserNotAuthenticated() {
        // Arrange
        when(session.getAttribute("user")).thenReturn(null);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L, session);

        // Assert
        assertEquals(ResponseEntity.status(403).build(), response);
        verify(userService, never()).deleteUser(1L);
    }
}

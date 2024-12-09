package com.example.usermicroservice.service;

import com.example.usermicroservice.entity.User;
import com.example.usermicroservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setRole("USER");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setRole("ADMIN");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(2, users.size());
        assertEquals("User One", users.get(0).getName());
        assertEquals("User Two", users.get(1).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserByIdWhenUserExists() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setName("User One");
        user.setEmail("user1@example.com");
        user.setPassword("password1");
        user.setRole("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        Optional<User> foundUser = userService.getUserById(1L);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("User One", foundUser.get().getName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserByIdWhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<User> foundUser = userService.getUserById(1L);

        // Assert
        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateUser() {
        // Arrange
        User user = new User();
        user.setName("New User");
        user.setEmail("newuser@example.com");
        user.setPassword("newpassword");
        user.setRole("USER");

        when(userRepository.save(user)).thenReturn(user);

        // Act
        User createdUser = userService.createUser(user);

        // Assert
        assertNotNull(createdUser);
        assertEquals("New User", createdUser.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUserWhenUserExists() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Existing User");
        existingUser.setEmail("existinguser@example.com");
        existingUser.setPassword("password");
        existingUser.setRole("USER");

        User updatedUser = new User();
        updatedUser.setName("Updated User");
        updatedUser.setEmail("updateduser@example.com");
        updatedUser.setPassword("newpassword");
        updatedUser.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Act
        User result = userService.updateUser(1L, updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals("Updated User", result.getName());
        assertEquals("updateduser@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testUpdateUserWhenUserDoesNotExist() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setName("Updated User");
        updatedUser.setEmail("updateduser@example.com");
        updatedUser.setPassword("newpassword");
        updatedUser.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, updatedUser));
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }
}

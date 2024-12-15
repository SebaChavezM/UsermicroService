package com.example.usermicroservice.auth;

import com.example.usermicroservice.entity.User;
import com.example.usermicroservice.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpSession session;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Successful() {
        String email = "test@example.com";
        String password = "password123";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(password);
        mockUser.setRole("USER");

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        ResponseEntity<Map<String, String>> response = authController.login(loginRequest, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login exitoso", response.getBody().get("message"));
        assertEquals("USER", response.getBody().get("role"));
        verify(session).setAttribute("user", mockUser);
    }

    @Test
    void testLogin_InvalidCredentials() {
        String email = "test@example.com";
        String password = "wrongPassword";

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, String>> response = authController.login(loginRequest, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Credenciales incorrectas", response.getBody().get("message"));
    }

    @Test
    void testCheckSession_ValidSession() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("John Doe");
        mockUser.setEmail("test@example.com");
        mockUser.setRole("USER");

        when(session.getAttribute("user")).thenReturn(mockUser);

        ResponseEntity<Map<String, Object>> response = authController.checkSession(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("authenticated"));
        Map<String, Object> user = (Map<String, Object>) response.getBody().get("user");
        assertEquals("John Doe", user.get("name"));
        assertEquals("test@example.com", user.get("email"));
    }

    @Test
    void testCheckSession_InvalidSession() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = authController.checkSession(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("authenticated"));
        assertEquals("No hay sesión activa", response.getBody().get("message"));
    }

    @Test
    void testLogout() {
        ResponseEntity<Map<String, String>> response = authController.logout(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sesión cerrada", response.getBody().get("message"));
        verify(session).invalidate();
    }

    @Test
    void testRegister_Successful() {
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());

        ResponseEntity<Map<String, String>> response = authController.register(registerRequest, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Usuario registrado con éxito", response.getBody().get("message"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(new User()));

        ResponseEntity<Map<String, String>> response = authController.register(registerRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El correo ya está registrado.", response.getBody().get("message"));
    }

    @Test
    void testRegister_ValidationError() {
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setName("");
        registerRequest.setEmail("invalid-email");
        registerRequest.setPassword("");

        when(bindingResult.hasErrors()).thenReturn(true);

        // Simular FieldError para evitar NullPointerException
        org.springframework.validation.FieldError fieldError = mock(org.springframework.validation.FieldError.class);
        when(bindingResult.getFieldError()).thenReturn(fieldError);
        when(fieldError.getDefaultMessage()).thenReturn("Datos inválidos");

        ResponseEntity<Map<String, String>> response = authController.register(registerRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Datos inválidos", response.getBody().get("message"));
    }

    @Test
    void testForgotPassword_EmailExists() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ResponseEntity<Map<String, String>> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Se ha enviado un correo para restablecer tu contraseña.", response.getBody().get("message"));
    }

    @Test
    void testForgotPassword_EmailNotExists() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ResponseEntity<Map<String, String>> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("El correo no está registrado.", response.getBody().get("message"));
    }
}

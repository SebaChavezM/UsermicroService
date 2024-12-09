package com.example.usermicroservice.auth;

import com.example.usermicroservice.entity.User;
import com.example.usermicroservice.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

        assertEquals(200, response.getStatusCodeValue());
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

        assertEquals(401, response.getStatusCodeValue());
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

        assertEquals(200, response.getStatusCodeValue());
        assertTrue((Boolean) response.getBody().get("authenticated"));
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) response.getBody().get("user");
        assertEquals("John Doe", user.get("name"));
    }

    @Test
    void testCheckSession_InvalidSession() {
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = authController.checkSession(session);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse((Boolean) response.getBody().get("authenticated"));
        assertEquals("No hay sesión activa", response.getBody().get("message"));
    }

    @Test
    void testLogout() {
        ResponseEntity<Map<String, String>> response = authController.logout(session);

        assertEquals(200, response.getStatusCodeValue());
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

        assertEquals(200, response.getStatusCodeValue());
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

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("El correo ya está registrado.", response.getBody().get("message"));
    }

    @Test
    void testRegister_ValidationError() {
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setName("");
        registerRequest.setEmail("invalid-email");
        registerRequest.setPassword("");

        when(bindingResult.hasErrors()).thenReturn(true);
        org.springframework.validation.FieldError fieldError = mock(org.springframework.validation.FieldError.class);
        when(bindingResult.getFieldError()).thenReturn(fieldError);
        when(fieldError.getDefaultMessage()).thenReturn("Datos inválidos");

        ResponseEntity<Map<String, String>> response = authController.register(registerRequest, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Datos inválidos", response.getBody().get("message"));
    }

    @Test
    void testForgotPassword_EmailExists() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ResponseEntity<Map<String, String>> response = authController.forgotPassword(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Se ha enviado un correo para restablecer tu contraseña.", response.getBody().get("message"));
    }

    @Test
    void testForgotPassword_EmailNotExists() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ResponseEntity<Map<String, String>> response = authController.forgotPassword(request);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("El correo no está registrado.", response.getBody().get("message"));
    }
}

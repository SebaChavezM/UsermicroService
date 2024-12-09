package com.example.usermicroservice.auth;

import com.example.usermicroservice.entity.User;
import com.example.usermicroservice.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
        if (user.isPresent() && user.get().getPassword().equals(loginRequest.getPassword())) {
            // Guardar el usuario en la sesión
            session.setAttribute("user", user.get());

            // Crear respuesta de éxito
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login exitoso");
            response.put("role", user.get().getRole());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(Map.of("message", "Credenciales incorrectas"));
    }

    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            // Si la sesión es válida, devolver los datos del usuario
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                )
            ));
        }
        return ResponseEntity.status(401).body(Map.of(
            "authenticated", false,
            "message", "No hay sesión activa"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        // Invalidar la sesión
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
    }

    static class LoginRequest {
        private String email;
        private String password;

        // Getters y setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest registerRequest, BindingResult bindingResult) {
        // Manejar errores de validación
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of("message", errorMessage));
        }

        // Validar si el correo ya está registrado
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El correo ya está registrado."));
        }

        // Crear un nuevo usuario
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); // Asegúrate de usar encriptación para la contraseña
        user.setRole("USER"); // Puedes ajustar el rol predeterminado

        // Guardar en la base de datos
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Usuario registrado con éxito"));
    }

    // Clase DTO para el registro
    static class RegisterRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String name;

        @Email(message = "El correo no es válido")
        @NotBlank(message = "El correo es obligatorio")
        private String email;

        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        @NotBlank(message = "La contraseña es obligatoria")
        private String password;

        // Getters y setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Se ha enviado un correo para restablecer tu contraseña."));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "El correo no está registrado."));
        }
    }

}

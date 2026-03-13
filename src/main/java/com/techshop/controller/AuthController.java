package com.techshop.controller;

import com.techshop.model.Usuario;
import com.techshop.repository.UsuarioRepository;
import com.techshop.util.Token;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
    private final Token tokenUtil;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor con inyección
    public AuthController(
            AuthenticationManager authenticationManager,
            Token tokenUtil,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenUtil = tokenUtil;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

   
    
    static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario nuevoUsuario) {
        if (usuarioRepository.existsByEmail(nuevoUsuario.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "El email ya está en uso"));
        }
        
        nuevoUsuario.setPassword(passwordEncoder.encode(nuevoUsuario.getPassword()));
        
        // ASIGNACIÓN OBLIGATORIA DEL ROL
        if(nuevoUsuario.getRoles().isEmpty()){
            nuevoUsuario.getRoles().add(Usuario.Role.USER);
        }

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok(Map.of("message", "Usuario creado con éxito"));
    }
}
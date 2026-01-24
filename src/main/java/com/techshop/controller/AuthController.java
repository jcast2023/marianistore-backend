package com.techshop.controller;

import com.techshop.model.Usuario;
import com.techshop.repository.UsuarioRepository;
import com.techshop.util.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private Token tokenUtil;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            Usuario usuario = usuarioRepository.findOneByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<String> roles = usuario.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());

            String token = tokenUtil.crearToken(usuario.getIdUsuario(), usuario.getNombre(), email, roles);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Credenciales incorrectas"));
        }
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
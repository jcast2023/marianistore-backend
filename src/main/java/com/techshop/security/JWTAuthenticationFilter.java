package com.techshop.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techshop.util.Token;
import com.techshop.repository.UsuarioRepository;
import com.techshop.model.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final Token token;
    private final UsuarioRepository usuarioRepository;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, Token token, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.token = token;
        this.usuarioRepository = usuarioRepository;
        setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
            throws AuthenticationException {
        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (IOException e) {
            throw new RuntimeException("Fallo al leer las credenciales");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String email = userDetails.getUsername();
        
        
        Usuario usuario = usuarioRepository.findOneByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<String> roles = authResult.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());

        
        String tokenValue = token.crearToken(usuario.getIdUsuario(), usuario.getNombre(), email, roles);

        response.addHeader("Authorization", "Bearer " + tokenValue);
        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + tokenValue + "\"}");
        response.getWriter().flush();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) 
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Credenciales inválidas\"}");
        response.getWriter().flush();
    }

    static class LoginRequest {
        private String email;
        private String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
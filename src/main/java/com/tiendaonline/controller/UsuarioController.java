package com.tiendaonline.controller;

import com.tiendaonline.dto.UsuarioDTO;
import com.tiendaonline.dto.UsuarioPasswordDTO;
import com.tiendaonline.dto.UsuarioRegistroDTO;
import com.tiendaonline.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    
    @GetMapping
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Integer id) {
        Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerPorId(id);
        return usuarioOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    
    @GetMapping("/email")
    public ResponseEntity<UsuarioDTO> obtenerPorEmail(@RequestParam String email) {
        Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerPorEmail(email);
        return usuarioOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

   
    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody UsuarioRegistroDTO usuarioRegistroDTO) {
        UsuarioDTO nuevo = usuarioService.crearUsuario(usuarioRegistroDTO);
        return ResponseEntity.status(201).body(nuevo);
    }

   
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Integer id, @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO actualizado = usuarioService.actualizarUsuario(id, usuarioDTO);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> actualizarPassword(
            @PathVariable Integer id,
            @RequestBody UsuarioPasswordDTO dto) {

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailAuth = auth.getName();

        Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerPorId(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UsuarioDTO usuario = usuarioOpt.get();

        
        boolean esMismoUsuario = usuario.getEmail().equals(emailAuth);
        boolean esAdmin = auth.getAuthorities().stream()
                              .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!esMismoUsuario && !esAdmin) {
            
            return ResponseEntity.status(403).build();
        }

       
        boolean actualizado = usuarioService.actualizarPassword(id, dto.getNuevaPassword());
        if (!actualizado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }



}

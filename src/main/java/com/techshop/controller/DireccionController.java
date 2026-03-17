package com.techshop.controller;

import com.techshop.dto.DireccionDTO;
import com.techshop.service.DireccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/direcciones")
@CrossOrigin(origins = "http://localhost:4200")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<DireccionDTO>> listarDirecciones() {
        List<DireccionDTO> direcciones = direccionService.listarDirecciones();
        return ResponseEntity.ok(direcciones);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<DireccionDTO> obtenerPorId(@PathVariable Integer id) {
        Optional<DireccionDTO> direccionOpt = direccionService.obtenerPorId(id);
        return direccionOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<DireccionDTO>> obtenerPorUsuarioId(@PathVariable Integer idUsuario) {
        List<DireccionDTO> direcciones = direccionService.obtenerPorUsuarioId(idUsuario);
        return ResponseEntity.ok(direcciones);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<DireccionDTO> crearDireccion(@Valid @RequestBody DireccionDTO dto) {
        DireccionDTO nueva = direccionService.crearDireccion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<DireccionDTO> actualizarDireccion(
            @PathVariable Integer id, 
            @Valid @RequestBody DireccionDTO dto) {
        DireccionDTO actualizada = direccionService.actualizarDireccion(id, dto);
        if (actualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizada);
    }

    // ← MÉTODO ACTUALIZADO CON MANEJO DE ERRORES
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<?> eliminarDireccion(@PathVariable Integer id) {
        try {
            direccionService.eliminarDireccion(id);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalStateException e) {
            // Error cuando hay pedidos asociados
            Map<String, String> error = new HashMap<>();
            error.put("error", "CONSTRAINT_VIOLATION");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            
        } catch (RuntimeException e) {
            // Error cuando la dirección no existe
            Map<String, String> error = new HashMap<>();
            error.put("error", "NOT_FOUND");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            
        } catch (Exception e) {
            // Error genérico
            Map<String, String> error = new HashMap<>();
            error.put("error", "INTERNAL_ERROR");
            error.put("message", "No se pudo eliminar la dirección");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
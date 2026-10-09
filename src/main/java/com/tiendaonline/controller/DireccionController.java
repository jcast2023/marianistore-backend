package com.tiendaonline.controller;

import com.tiendaonline.dto.DireccionDTO;
import com.tiendaonline.service.DireccionService;
import com.tiendaonline.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    private final UsuarioService usuarioService;

    public DireccionController(DireccionService direccionService, UsuarioService usuarioService) {
        this.direccionService = direccionService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<DireccionDTO>> listarDirecciones(Authentication authentication) {
        // Si es admin, ve todas. Si no, solo las suyas.
        boolean isAdmin = esAdmin(authentication);
        if (isAdmin) {
            return ResponseEntity.ok(direccionService.listarDirecciones());
        }
        Integer miId = obtenerMiIdUsuario(authentication);
        return ResponseEntity.ok(direccionService.obtenerPorUsuarioId(miId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<DireccionDTO> obtenerPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        Optional<DireccionDTO> direccionOpt = direccionService.obtenerPorId(id);
        if (direccionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Verificar propiedad
        if (!esAdmin(authentication) &&
                !direccionOpt.get().getIdUsuario().equals(obtenerMiIdUsuario(authentication))) {
            throw new AccessDeniedException("No tienes permiso para acceder a esta dirección");
        }
        return ResponseEntity.ok(direccionOpt.get());
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<DireccionDTO>> obtenerPorUsuarioId(
            @PathVariable Integer idUsuario,
            Authentication authentication) {
        validarPropiedad(idUsuario, authentication);
        List<DireccionDTO> direcciones = direccionService.obtenerPorUsuarioId(idUsuario);
        return ResponseEntity.ok(direcciones);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<DireccionDTO> crearDireccion(
            @Valid @RequestBody DireccionDTO dto,
            Authentication authentication) {
        // Forzar que el idUsuario sea el autenticado (a menos que sea admin)
        if (!esAdmin(authentication)) {
            dto.setIdUsuario(obtenerMiIdUsuario(authentication));
        }
        DireccionDTO nueva = direccionService.crearDireccion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<DireccionDTO> actualizarDireccion(
            @PathVariable Integer id,
            @Valid @RequestBody DireccionDTO dto,
            Authentication authentication) {
        Optional<DireccionDTO> actual = direccionService.obtenerPorId(id);
        if (actual.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!esAdmin(authentication) &&
                !actual.get().getIdUsuario().equals(obtenerMiIdUsuario(authentication))) {
            throw new AccessDeniedException("No tienes permiso para modificar esta dirección");
        }
        DireccionDTO actualizada = direccionService.actualizarDireccion(id, dto);
        if (actualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<?> eliminarDireccion(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            // Verificar propiedad antes de eliminar
            Optional<DireccionDTO> direccionOpt = direccionService.obtenerPorId(id);
            if (direccionOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "NOT_FOUND");
                error.put("message", "La dirección no existe");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            if (!esAdmin(authentication) &&
                    !direccionOpt.get().getIdUsuario().equals(obtenerMiIdUsuario(authentication))) {
                throw new AccessDeniedException("No tienes permiso para eliminar esta dirección");
            }

            direccionService.eliminarDireccion(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "CONSTRAINT_VIOLATION");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

        } catch (AccessDeniedException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "FORBIDDEN");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "NOT_FOUND");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Integer obtenerMiIdUsuario(Authentication authentication) {
        String emailAuth = authentication.getName();
        return usuarioService.obtenerPorEmail(emailAuth)
                .map(u -> u.getIdUsuario())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado"));
    }

    private void validarPropiedad(Integer idUsuario, Authentication authentication) {
        if (esAdmin(authentication)) return;
        if (!idUsuario.equals(obtenerMiIdUsuario(authentication))) {
            throw new AccessDeniedException("No tienes permiso para acceder a los datos de otro usuario");
        }
    }
}
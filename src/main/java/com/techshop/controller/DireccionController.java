package com.techshop.controller;

import com.techshop.dto.DireccionDTO;
import com.techshop.service.DireccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @GetMapping
    public List<DireccionDTO> listarDirecciones() {
        return direccionService.listarDirecciones();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DireccionDTO> obtenerPorId(@PathVariable Integer id) {
        Optional<DireccionDTO> direccionOpt = direccionService.obtenerPorId(id);
        return direccionOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<DireccionDTO> obtenerPorUsuarioId(@PathVariable Integer idUsuario) {
        return direccionService.obtenerPorUsuarioId(idUsuario);
    }

    @PostMapping
    public ResponseEntity<DireccionDTO> crearDireccion(@RequestBody DireccionDTO dto) {
        DireccionDTO nueva = direccionService.crearDireccion(dto);
        return ResponseEntity.status(201).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DireccionDTO> actualizarDireccion(@PathVariable Integer id, @RequestBody DireccionDTO dto) {
        DireccionDTO actualizada = direccionService.actualizarDireccion(id, dto);
        if (actualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable Integer id) {
        direccionService.eliminarDireccion(id);
        return ResponseEntity.noContent().build();
    }
}

package com.techshop.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.techshop.dto.CarritoDTO;
import com.techshop.service.CarritoService;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public List<CarritoDTO> listarCarritos() {
        return carritoService.listarCarritos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoDTO> obtenerCarritoPorId(@PathVariable Integer id) {
        Optional<CarritoDTO> carritoOpt = carritoService.obtenerPorId(id);
        return carritoOpt.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CarritoDTO> crearCarrito(@RequestBody CarritoDTO carritoDTO) {
        CarritoDTO nuevoCarrito = carritoService.crearCarrito(carritoDTO);
        return ResponseEntity.status(201).body(nuevoCarrito);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarritoDTO> actualizarCarrito(@PathVariable Integer id, @RequestBody CarritoDTO carritoDTO) {
        CarritoDTO actualizado = carritoService.actualizarCarrito(id, carritoDTO);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCarrito(@PathVariable Integer id) {
        carritoService.eliminarCarrito(id);
        return ResponseEntity.noContent().build();
    }
}

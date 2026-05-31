package com.tiendaonline.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tiendaonline.dto.CarritoDTO;
import com.tiendaonline.dto.ItemcarritoDTO;
import com.tiendaonline.service.CarritoService;

@RestController
@RequestMapping("/api/carritos")
@CrossOrigin(origins = "http://localhost:4200")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // ── CRUD básico ───────────────────────────────────────────────

    @GetMapping
    public List<CarritoDTO> listarCarritos() {
        return carritoService.listarCarritos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoDTO> obtenerCarritoPorId(@PathVariable Integer id) {
        return carritoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CarritoDTO> crearCarrito(@RequestBody CarritoDTO carritoDTO) {
        return ResponseEntity.status(201).body(carritoService.crearCarrito(carritoDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarritoDTO> actualizarCarrito(
            @PathVariable Integer id, @RequestBody CarritoDTO carritoDTO) {
        CarritoDTO actualizado = carritoService.actualizarCarrito(id, carritoDTO);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCarrito(@PathVariable Integer id) {
        carritoService.eliminarCarrito(id);
        return ResponseEntity.noContent().build();
    }

    // ── Carrito persistente por usuario ──────────────────────────

    /** Obtiene o crea el carrito activo del usuario */
    @GetMapping("/usuario/{idUsuario}/activo")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<CarritoDTO> obtenerCarritoActivo(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(carritoService.obtenerOCrearCarritoActivo(idUsuario));
    }

    /** Lista los items del carrito activo del usuario */
    @GetMapping("/usuario/{idUsuario}/items")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<ItemcarritoDTO>> obtenerItems(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(carritoService.obtenerItemsCarrito(idUsuario));
    }

    /** Agrega un producto al carrito */
    @PostMapping("/usuario/{idUsuario}/items")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<ItemcarritoDTO> agregarItem(
            @PathVariable Integer idUsuario,
            @RequestBody ItemcarritoDTO itemDTO) {
        ItemcarritoDTO creado = carritoService.agregarItem(
                idUsuario, itemDTO.getIdProducto(), itemDTO.getCantidad());
        return ResponseEntity.status(201).body(creado);
    }

    /** Actualiza la cantidad de un item */
    @PutMapping("/items/{idItem}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<ItemcarritoDTO> actualizarItem(
            @PathVariable Integer idItem,
            @RequestBody ItemcarritoDTO itemDTO) {
        return ResponseEntity.ok(
                carritoService.actualizarCantidadItem(idItem, itemDTO.getCantidad()));
    }

    /** Elimina un item del carrito */
    @DeleteMapping("/items/{idItem}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> eliminarItem(@PathVariable Integer idItem) {
        carritoService.eliminarItem(idItem);
        return ResponseEntity.noContent().build();
    }

    /** Vacía todo el carrito del usuario */
    @DeleteMapping("/usuario/{idUsuario}/vaciar")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Integer idUsuario) {
        carritoService.vaciarCarrito(idUsuario);
        return ResponseEntity.noContent().build();
    }

    /** Sincroniza el carrito desde localStorage al hacer login */
    @PostMapping("/usuario/{idUsuario}/sincronizar")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> sincronizar(
            @PathVariable Integer idUsuario,
            @RequestBody List<ItemcarritoDTO> items) {
        carritoService.sincronizarDesdeLocalStorage(idUsuario, items);
        return ResponseEntity.ok().build();
    }
}
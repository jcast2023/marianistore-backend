package com.tiendaonline.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tiendaonline.dto.WishlistDTO;
import com.tiendaonline.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:4200")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /** Obtener todos los favoritos de un usuario */
    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<WishlistDTO>> obtenerFavoritos(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(wishlistService.obtenerFavoritosPorUsuario(idUsuario));
    }

    /** Agregar un producto a favoritos */
    @PostMapping("/usuario/{idUsuario}/producto/{idProducto}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<WishlistDTO> agregarFavorito(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idProducto) {
        return ResponseEntity.status(201).body(
                wishlistService.agregarFavorito(idUsuario, idProducto));
    }

    /** Eliminar un producto de favoritos */
    @DeleteMapping("/usuario/{idUsuario}/producto/{idProducto}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> eliminarFavorito(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idProducto) {
        wishlistService.eliminarFavorito(idUsuario, idProducto);
        return ResponseEntity.noContent().build();
    }

    /** Verificar si un producto es favorito */
    @GetMapping("/usuario/{idUsuario}/producto/{idProducto}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Boolean> esFavorito(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idProducto) {
        return ResponseEntity.ok(wishlistService.esFavorito(idUsuario, idProducto));
    }

    /** Limpiar todos los favoritos de un usuario */
    @DeleteMapping("/usuario/{idUsuario}/limpiar")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> limpiarFavoritos(@PathVariable Integer idUsuario) {
        wishlistService.limpiarFavoritos(idUsuario);
        return ResponseEntity.noContent().build();
    }

    /** Sincronizar favoritos desde localStorage al hacer login */
    @PostMapping("/usuario/{idUsuario}/sincronizar")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> sincronizar(
            @PathVariable Integer idUsuario,
            @RequestBody List<Integer> idProductos) {
        wishlistService.sincronizarDesdeLocalStorage(idUsuario, idProductos);
        return ResponseEntity.ok().build();
    }
}
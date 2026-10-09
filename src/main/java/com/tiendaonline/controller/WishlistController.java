package com.tiendaonline.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import com.tiendaonline.dto.WishlistDTO;
import com.tiendaonline.service.WishlistService;
import com.tiendaonline.service.UsuarioService;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:4200")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UsuarioService usuarioService;

    public WishlistController(WishlistService wishlistService, UsuarioService usuarioService) {
        this.wishlistService = wishlistService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<WishlistDTO>> obtenerFavoritos(
            @PathVariable Integer idUsuario,
            Authentication authentication) {
        validarPropiedad(idUsuario, authentication);
        return ResponseEntity.ok(wishlistService.obtenerFavoritosPorUsuario(idUsuario));
    }

    @PostMapping("/usuario/{idUsuario}/producto/{idProducto}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<WishlistDTO> agregarFavorito(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idProducto,
            Authentication authentication) {
        validarPropiedad(idUsuario, authentication);
        return ResponseEntity.status(201).body(
                wishlistService.agregarFavorito(idUsuario, idProducto));
    }

    @DeleteMapping("/usuario/{idUsuario}/producto/{idProducto}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> eliminarFavorito(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idProducto,
            Authentication authentication) {
        validarPropiedad(idUsuario, authentication);
        wishlistService.eliminarFavorito(idUsuario, idProducto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{idUsuario}/producto/{idProducto}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Boolean> esFavorito(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idProducto,
            Authentication authentication) {
        validarPropiedad(idUsuario, authentication);
        return ResponseEntity.ok(wishlistService.esFavorito(idUsuario, idProducto));
    }

    @DeleteMapping("/usuario/{idUsuario}/limpiar")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> limpiarFavoritos(
            @PathVariable Integer idUsuario,
            Authentication authentication) {
        validarPropiedad(idUsuario, authentication);
        wishlistService.limpiarFavoritos(idUsuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuario/{idUsuario}/sincronizar")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<Void> sincronizar(
            @PathVariable Integer idUsuario,
            @RequestBody List<Integer> idProductos,
            Authentication authentication) {
        validarPropiedad(idUsuario, authentication);
        wishlistService.sincronizarDesdeLocalStorage(idUsuario, idProductos);
        return ResponseEntity.ok().build();
    }

    private void validarPropiedad(Integer idUsuario, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;

        String emailAuth = authentication.getName();
        var usuarioOpt = usuarioService.obtenerPorEmail(emailAuth);
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getIdUsuario().equals(idUsuario)) {
            throw new AccessDeniedException(
                    "No tienes permiso para acceder a los datos de otro usuario");
        }
    }
}
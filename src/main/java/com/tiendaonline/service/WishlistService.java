// ══════════════════════════════════════════════════════
// WishlistService.java — Interface
// ══════════════════════════════════════════════════════
package com.tiendaonline.service;

import java.util.List;
import com.tiendaonline.dto.WishlistDTO;

public interface WishlistService {
    List<WishlistDTO> obtenerFavoritosPorUsuario(Integer idUsuario);
    WishlistDTO       agregarFavorito(Integer idUsuario, Integer idProducto);
    void              eliminarFavorito(Integer idUsuario, Integer idProducto);
    void              limpiarFavoritos(Integer idUsuario);
    boolean           esFavorito(Integer idUsuario, Integer idProducto);
    void              sincronizarDesdeLocalStorage(Integer idUsuario, List<Integer> idProductos);
}
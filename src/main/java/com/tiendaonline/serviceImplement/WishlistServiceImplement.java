package com.tiendaonline.serviceImplement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.dto.WishlistDTO;
import com.tiendaonline.model.Producto;
import com.tiendaonline.model.Usuario;
import com.tiendaonline.model.Wishlist;
import com.tiendaonline.repository.ProductoRepository;
import com.tiendaonline.repository.UsuarioRepository;
import com.tiendaonline.repository.WishlistRepository;
import com.tiendaonline.service.WishlistService;

@Service
public class WishlistServiceImplement implements WishlistService {

    private final WishlistRepository  wishlistRepository;
    private final UsuarioRepository   usuarioRepository;
    private final ProductoRepository  productoRepository;

    public WishlistServiceImplement(
            WishlistRepository wishlistRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.wishlistRepository = wishlistRepository;
        this.usuarioRepository  = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<WishlistDTO> obtenerFavoritosPorUsuario(Integer idUsuario) {
        return wishlistRepository.findByUsuario_IdUsuario(idUsuario)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WishlistDTO agregarFavorito(Integer idUsuario, Integer idProducto) {
        // Si ya existe, devolver el existente sin duplicar
        return wishlistRepository
                .findByUsuario_IdUsuarioAndProducto_IdProducto(idUsuario, idProducto)
                .map(this::mapToDTO)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(idUsuario)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
                    Producto producto = productoRepository.findById(idProducto)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + idProducto));

                    Wishlist wishlist = new Wishlist();
                    wishlist.setUsuario(usuario);
                    wishlist.setProducto(producto);

                    return mapToDTO(wishlistRepository.save(wishlist));
                });
    }

    @Override
    @Transactional
    public void eliminarFavorito(Integer idUsuario, Integer idProducto) {
        wishlistRepository.deleteByUsuario_IdUsuarioAndProducto_IdProducto(idUsuario, idProducto);
    }

    @Override
    @Transactional
    public void limpiarFavoritos(Integer idUsuario) {
        wishlistRepository.deleteByUsuario_IdUsuario(idUsuario);
    }

    @Override
    public boolean esFavorito(Integer idUsuario, Integer idProducto) {
        return wishlistRepository.existsByUsuario_IdUsuarioAndProducto_IdProducto(idUsuario, idProducto);
    }

    @Override
    @Transactional
    public void sincronizarDesdeLocalStorage(Integer idUsuario, List<Integer> idProductos) {
        // Agrega los productos del localStorage que no estén ya en BD
        for (Integer idProducto : idProductos) {
            if (!wishlistRepository.existsByUsuario_IdUsuarioAndProducto_IdProducto(idUsuario, idProducto)) {
                try {
                    agregarFavorito(idUsuario, idProducto);
                } catch (RuntimeException e) {
                    // Si el producto no existe, ignorar
                    System.err.println("Producto no encontrado al sincronizar wishlist: " + idProducto);
                }
            }
        }
    }

    private WishlistDTO mapToDTO(Wishlist w) {
        WishlistDTO dto = new WishlistDTO();
        dto.setIdWishlist(w.getIdWishlist());
        dto.setIdUsuario(w.getUsuario().getIdUsuario());
        dto.setIdProducto(w.getProducto().getIdProducto());
        dto.setNombreProducto(w.getProducto().getNombre());
        dto.setImagenProducto(w.getProducto().getImagen());
        dto.setPrecioProducto(w.getProducto().getPrecio());
        dto.setStockProducto(w.getProducto().getStock());
        dto.setFechaAgregado(w.getFechaAgregado());
        if (w.getProducto().getCategoria() != null) {
            dto.setCategoriaProducto(w.getProducto().getCategoria().getNombre());
        }
        return dto;
    }
}
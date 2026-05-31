package com.tiendaonline.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiendaonline.model.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    // Todos los favoritos de un usuario
    List<Wishlist> findByUsuario_IdUsuario(Integer idUsuario);

    // Buscar un item específico
    Optional<Wishlist> findByUsuario_IdUsuarioAndProducto_IdProducto(
            Integer idUsuario, Integer idProducto);

    // Verificar si existe
    boolean existsByUsuario_IdUsuarioAndProducto_IdProducto(
            Integer idUsuario, Integer idProducto);

    // Eliminar un item específico
    void deleteByUsuario_IdUsuarioAndProducto_IdProducto(
            Integer idUsuario, Integer idProducto);

    // Eliminar todos los favoritos de un usuario
    void deleteByUsuario_IdUsuario(Integer idUsuario);
}
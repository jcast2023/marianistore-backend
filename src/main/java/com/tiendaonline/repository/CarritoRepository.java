package com.tiendaonline.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiendaonline.model.Carrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    // Busca el carrito ACTIVO de un usuario
    Optional<Carrito> findByUsuario_IdUsuarioAndEstado(Integer idUsuario, String estado);

    // Busca cualquier carrito del usuario
    Optional<Carrito> findByUsuario_IdUsuario(Integer idUsuario);
}
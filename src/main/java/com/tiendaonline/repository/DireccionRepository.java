package com.tiendaonline.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiendaonline.model.Direccion;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion,Integer>{

	List<Direccion> findByUsuarioIdUsuario(Integer idUsuario);
}

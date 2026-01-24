package com.techshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techshop.model.Carrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito,Integer> {

	
}

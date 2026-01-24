package com.techshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.techshop.model.Itemcarrito;

@Repository
public interface ItemcarritoRepository extends JpaRepository<Itemcarrito,Integer>{

	List<Itemcarrito> findByCarrito_IdCarrito(Integer idCarrito);

}

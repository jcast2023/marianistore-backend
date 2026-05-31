package com.tiendaonline.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tiendaonline.model.Itempedido;

@Repository
public interface ItempedidoRepository extends JpaRepository<Itempedido,Integer> {

	List<Itempedido> findByPedidoIdPedido(Integer idPedido);

}

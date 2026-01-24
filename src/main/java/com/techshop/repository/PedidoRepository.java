package com.techshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.techshop.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido,Integer> {


	@Query("SELECT DISTINCT p FROM Pedido p WHERE p.usuario.email = :email ORDER BY p.fechaPedido DESC")
	List<Pedido> findByUsuarioEmail(@Param("email") String email);
	
	// PedidoRepository.java
	@Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado IN ('PAGADO', 'ENVIADO')")
	Double sumarVentasTotales();

	@Query("SELECT COUNT(p) FROM Pedido p WHERE p.estado = 'PAGADO'")
	Long contarPedidosPorEnviar();
}

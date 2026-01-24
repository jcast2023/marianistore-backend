package com.techshop.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.techshop.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto,Integer> {

	
	@Query("SELECT COUNT(p) FROM Producto p WHERE p.stock < 5")
	long countStockCritico();
	Long countByStockLessThan(Integer limite);
	@Query("SELECT i.producto, SUM(i.cantidad) as total " +
		       "FROM Itempedido i " +
		       "GROUP BY i.producto " +
		       "ORDER BY total DESC")
		List<Object[]> findTop5BestSellers(Pageable pageable);
}

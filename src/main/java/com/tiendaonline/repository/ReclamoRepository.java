package com.tiendaonline.repository;

import com.tiendaonline.model.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    Optional<Reclamo> findByCodigoReclamo(String codigoReclamo);
}

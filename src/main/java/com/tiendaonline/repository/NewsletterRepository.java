package com.tiendaonline.repository;

import com.tiendaonline.model.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {
    
    Optional<Newsletter> findByEmail(String email);
    
}
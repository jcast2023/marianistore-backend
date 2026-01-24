package com.techshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techshop.model.Newsletter;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {
}

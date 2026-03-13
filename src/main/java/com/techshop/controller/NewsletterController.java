package com.techshop.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.techshop.model.Newsletter;
import com.techshop.repository.NewsletterRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/newsletter")
@CrossOrigin(origins = "http://localhost:4200")
public class NewsletterController {

    private final NewsletterRepository repository;

    
    public NewsletterController(NewsletterRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/suscribir")
    public ResponseEntity<?> suscribir(@Valid @RequestBody Newsletter newsletter) {

        
        String emailNormalizado = newsletter.getEmail().trim().toLowerCase();
        newsletter.setEmail(emailNormalizado);

        // Verificamos existencia antes de guardar 
        Optional<Newsletter> existente = repository.findByEmail(emailNormalizado);
        if (existente.isPresent()) {
            return ResponseEntity
                .badRequest()
                .body("El correo ya está registrado");
        }

        try {
            Newsletter guardado = repository.save(newsletter);
            
            
            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
           
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al procesar la suscripción. Intenta nuevamente.");
        }
    }
}
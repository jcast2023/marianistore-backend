package com.techshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techshop.model.Newsletter;
import com.techshop.repository.NewsletterRepository;

@RestController
@RequestMapping("/api/newsletter")
@CrossOrigin(origins = "http://localhost:4200") 
public class NewsletterController {

    @Autowired
    private NewsletterRepository repository;

    @PostMapping("/suscribir")
    public ResponseEntity<?> suscribir(@RequestBody Newsletter newsletter) {
        try {
            return ResponseEntity.ok(repository.save(newsletter));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("El correo ya está registrado o es inválido");
        }
    }
}

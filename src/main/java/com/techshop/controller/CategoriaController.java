package com.techshop.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.techshop.model.Categoria;
import com.techshop.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService; 

    @GetMapping
    public List<Categoria> listarTodas() {
        return categoriaService.listarTodas();
    }
}
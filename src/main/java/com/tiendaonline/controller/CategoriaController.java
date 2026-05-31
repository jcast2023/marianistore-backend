package com.tiendaonline.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tiendaonline.model.Categoria;
import com.tiendaonline.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
	    this.categoriaService = categoriaService;
	} 

	@GetMapping
	public ResponseEntity<List<Categoria>> listarTodas() {
	    return ResponseEntity.ok(categoriaService.listarTodas());
	}
}
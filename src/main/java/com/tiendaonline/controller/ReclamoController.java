package com.tiendaonline.controller;

import com.tiendaonline.dto.ReclamoDTO;
import com.tiendaonline.model.Reclamo;
import com.tiendaonline.service.ReclamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamaciones")
@CrossOrigin(origins = "http://localhost:4200") // Sincronizado con tu puerto local de Angular
public class ReclamoController {

    // Apuntamos directo a la interfaz
    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping
    public ResponseEntity<Reclamo> crearReclamo(@Valid @RequestBody ReclamoDTO reclamoDTO) {
        Reclamo nuevoReclamo = reclamoService.registrarReclamo(reclamoDTO);
        return new ResponseEntity<>(nuevoReclamo, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Reclamo>> obtenerTodosLosReclamos() {
        return ResponseEntity.ok(reclamoService.listarTodos());
    }

    @GetMapping("/buscar/{codigo}") // 🚀 CAMBIADO: De "/codigo/{codigo}" a "/buscar/{codigo}"
    public ResponseEntity<Reclamo> obtenerReclamoPorCodigo(@PathVariable String codigo) {
        return reclamoService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

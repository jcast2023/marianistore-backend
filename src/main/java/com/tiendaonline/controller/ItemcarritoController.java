package com.tiendaonline.controller;

import com.tiendaonline.dto.ItemcarritoDTO;
import com.tiendaonline.service.ItemcarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/itemcarrito")
public class ItemcarritoController {

    private final ItemcarritoService itemcarritoService;

    public ItemcarritoController(ItemcarritoService itemcarritoService) {
        this.itemcarritoService = itemcarritoService;
    }

    @GetMapping
    public List<ItemcarritoDTO> listarItemsCarrito() {
        return itemcarritoService.listarItemsCarrito();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemcarritoDTO> obtenerPorId(@PathVariable Integer id) {
        Optional<ItemcarritoDTO> itemOpt = itemcarritoService.obtenerPorId(id);
        return itemOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/carrito/{idCarrito}")
    public List<ItemcarritoDTO> obtenerPorCarritoId(@PathVariable Integer idCarrito) {
        return itemcarritoService.obtenerPorCarritoId(idCarrito);
    }

    @PostMapping
    public ResponseEntity<ItemcarritoDTO> crearItemCarrito(@RequestBody ItemcarritoDTO dto) {
        ItemcarritoDTO nuevo = itemcarritoService.crearItemCarrito(dto);
        return ResponseEntity.status(201).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemcarritoDTO> actualizarItemCarrito(@PathVariable Integer id, @RequestBody ItemcarritoDTO dto) {
        ItemcarritoDTO actualizado = itemcarritoService.actualizarItemCarrito(id, dto);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItemCarrito(@PathVariable Integer id) {
        itemcarritoService.eliminarItemCarrito(id);
        return ResponseEntity.noContent().build();
    }
}

package com.tiendaonline.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tiendaonline.dto.ItempedidoDTO;
import com.tiendaonline.service.ItempedidoService;

@RestController
@RequestMapping("/api/itempedidos")
public class ItempedidoController {

    private final ItempedidoService itempedidoService;

    public ItempedidoController(ItempedidoService itempedidoService) {
        this.itempedidoService = itempedidoService;
    }

    @GetMapping
    public List<ItempedidoDTO> listarItemsPedidos() {
        return itempedidoService.listarItemsPedidos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItempedidoDTO> obtenerItemPedidoPorId(@PathVariable Integer id) {
        Optional<ItempedidoDTO> itemOpt = itempedidoService.obtenerPorId(id);
        return itemOpt.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{idPedido}")
    public List<ItempedidoDTO> listarItemsPorPedido(@PathVariable Integer idPedido) {
        return itempedidoService.obtenerPorPedidoId(idPedido);
    }

    @PostMapping
    public ResponseEntity<ItempedidoDTO> crearItemPedido(@RequestBody ItempedidoDTO itempedidoDTO) {
        ItempedidoDTO nuevoItem = itempedidoService.crearItemPedido(itempedidoDTO);
        return ResponseEntity.status(201).body(nuevoItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItempedidoDTO> actualizarItemPedido(@PathVariable Integer id, @RequestBody ItempedidoDTO itempedidoDTO) {
        ItempedidoDTO actualizado = itempedidoService.actualizarItemPedido(id, itempedidoDTO);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItemPedido(@PathVariable Integer id) {
        itempedidoService.eliminarItemPedido(id);
        return ResponseEntity.noContent().build();
    }
}

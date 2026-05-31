package com.tiendaonline.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.dto.ProductoDTO;
import com.tiendaonline.service.ProductoService;


@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    
    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listar() {
        List<ProductoDTO> productos = productoService.listarProductos();
        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Integer id) {
        return productoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoDTO productoDTO) {
        ProductoDTO creado = productoService.crearProducto(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable Integer id, @RequestBody ProductoDTO productoDTO) {
        ProductoDTO actualizado = productoService.actualizarProducto(id, productoDTO);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
    	productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public ResponseEntity<ProductoDTO> actualizarStock(
            @PathVariable Integer id, 
            @RequestBody java.util.Map<String, Integer> body) { 
        
        Integer nuevaCantidad = body.get("stock");
        return ResponseEntity.ok(productoService.actualizarStock(id, nuevaCantidad));
    }

    
    @GetMapping("/stock-critico/count")
    @PreAuthorize("hasAuthority('ADMIN')") 
    public ResponseEntity<Long> obtenerConteoStockCritico() {
        return ResponseEntity.ok(productoService.contarProductosBajoStock());
    }
    
    @GetMapping("/top5")
    public ResponseEntity<List<ProductoDTO>> getTop5Vendidos() {
        return ResponseEntity.ok(productoService.obtenerTop5MasVendidos());
    }
}

package com.techshop.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.techshop.dto.PedidoDTO;
import com.techshop.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:4200")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> obtenerPedidoPorId(@PathVariable Integer id, Authentication authentication) {
        return pedidoService.obtenerPorId(id)
            .map(pedido -> {
                if (!esPropietarioOAdmin(pedido, authentication)) {
                    return ResponseEntity.status(403).<PedidoDTO>build();
                }
                return ResponseEntity.ok(pedido);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<List<PedidoDTO>> obtenerPedidosPorUsuarioId(
            @PathVariable Integer idUsuario, 
            Authentication authentication) {
        
        String emailLogueado = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        
        if (!isAdmin) {
            Optional<PedidoDTO> primerPedido = pedidoService.obtenerPorId(idUsuario);
        }
        
        List<PedidoDTO> pedidos = pedidoService.obtenerPedidosPorUsuarioId(idUsuario);
        return ResponseEntity.ok(pedidos);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')") 
    public ResponseEntity<PedidoDTO> crearPedido(@Valid @RequestBody PedidoDTO pedidoDTO) {
        PedidoDTO nuevoPedido = pedidoService.crearPedido(pedidoDTO);
        return ResponseEntity.status(201).body(nuevoPedido);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PedidoDTO> actualizarPedido(@PathVariable Integer id, @RequestBody PedidoDTO pedidoDTO) {
        PedidoDTO pedidoActualizado = pedidoService.actualizarPedido(id, pedidoDTO);
        if (pedidoActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pedidoActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Integer id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/pagar")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<PedidoDTO> pagarPedido(
            @PathVariable Integer id, 
            @RequestParam(required = false) String metodoPago,
            Authentication authentication) {
        
        Optional<PedidoDTO> pedidoOpt = pedidoService.obtenerPorId(id);
        if (pedidoOpt.isPresent() && !esPropietarioOAdmin(pedidoOpt.get(), authentication)) {
            return ResponseEntity.status(403).build();
        }
        
        PedidoDTO pedidoPagado = pedidoService.pagarPedido(id, metodoPago);
        return ResponseEntity.ok(pedidoPagado);
    }
    
    @GetMapping
    public ResponseEntity<List<PedidoDTO>> obtenerPedidos(Authentication authentication) {
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        List<PedidoDTO> pedidos;
        if (isAdmin) {
            pedidos = pedidoService.listarPedidos();
        } else {
            pedidos = pedidoService.obtenerPedidosPorUsuario(email);
        }
        
        return ResponseEntity.ok(pedidos);
    }
    
    // ← ACTUALIZADO: inline para ver en navegador
    @GetMapping("/{id}/factura")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public void descargarFactura(
            @PathVariable Integer id, 
            HttpServletResponse response, 
            Authentication authentication) throws IOException {
        
        Optional<PedidoDTO> pedidoOpt = pedidoService.obtenerPorId(id);
        
        if (pedidoOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado");
            return;
        }

        if (!esPropietarioOAdmin(pedidoOpt.get(), authentication)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para acceder a esta factura");
            return;
        }

        response.setContentType("application/pdf");
        // ← CAMBIAR A "inline" para ver en navegador (o "attachment" para descargar)
        response.setHeader("Content-Disposition", "inline; filename=factura_pedido_" + id + ".pdf");

        pedidoService.exportarFacturaPDF(id, response);
    }
    
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PedidoDTO> actualizarEstado(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");
        PedidoDTO pedidoActualizado = pedidoService.actualizarEstadoManual(id, nuevoEstado);
        
        if (pedidoActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pedidoActualizado);
    }

    private boolean esPropietarioOAdmin(PedidoDTO pedido, Authentication authentication) {
        String emailLogueado = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        
        return isAdmin || pedido.getEmailUsuario().equals(emailLogueado);
    }
}
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
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Obtener un pedido por ID con validación de propiedad.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> obtenerPedidoPorId(@PathVariable Integer id, Authentication authentication) {
        return pedidoService.obtenerPorId(id)
            .map(pedido -> {
                // VALIDACIÓN DE SEGURIDAD
                if (!esPropietarioOAdmin(pedido, authentication)) {
                    return ResponseEntity.status(403).<PedidoDTO>build();
                }
                return ResponseEntity.ok(pedido);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')") 
    public ResponseEntity<PedidoDTO> crearPedido(@Valid @RequestBody PedidoDTO pedidoDTO) {
        PedidoDTO nuevoPedido = pedidoService.crearPedido(pedidoDTO);
        return ResponseEntity.status(201).body(nuevoPedido);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo el admin debería editar pedidos generales
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
    public ResponseEntity<PedidoDTO> pagarPedido(@PathVariable Integer id, Authentication authentication) {
        // Validamos propiedad antes de permitir el pago
        Optional<PedidoDTO> pedidoOpt = pedidoService.obtenerPorId(id);
        if (pedidoOpt.isPresent() && !esPropietarioOAdmin(pedidoOpt.get(), authentication)) {
            return ResponseEntity.status(403).build();
        }
        
        PedidoDTO pedidoPagado = pedidoService.pagarPedido(id);
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
    
    /**
     * Descarga de factura PDF blindada.
     */
    @GetMapping("/{id}/factura")
    public void descargarFactura(@PathVariable Integer id, HttpServletResponse response, Authentication authentication) throws IOException {
        Optional<PedidoDTO> pedidoOpt = pedidoService.obtenerPorId(id);
        
        if (pedidoOpt.isEmpty()) {
            response.sendError(404, "Pedido no encontrado");
            return;
        }

        // VALIDACIÓN DE SEGURIDAD
        if (!esPropietarioOAdmin(pedidoOpt.get(), authentication)) {
            response.sendError(403, "No tienes permiso para acceder a esta factura.");
            return;
        }

        response.setContentType("application/pdf");
        String headerValue = "attachment; filename=factura_pedido_" + id + ".pdf";
        response.setHeader("Content-Disposition", headerValue);

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

    /**
     * MÉTODO PRIVADO REUTILIZABLE para validación de identidad.
     * Verifica si el usuario logueado es el dueño del recurso o tiene rol de administrador.
     */
    private boolean esPropietarioOAdmin(PedidoDTO pedido, Authentication authentication) {
        String emailLogueado = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        
        return isAdmin || pedido.getEmailUsuario().equals(emailLogueado);
    }
}
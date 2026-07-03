
package com.tiendaonline.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiendaonline.service.PedidoService;


import java.util.Map;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final PedidoService pedidoService;

	public AdminController(PedidoService pedidoService) {
	    this.pedidoService = pedidoService;
	}

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(pedidoService.obtenerEstadisticasDashboard());
    }
}
package com.tiendaonline.controller;


import com.tiendaonline.dto.PreferenciaRequestDTO;
import com.tiendaonline.dto.PreferenciaResponseDTO;
import com.tiendaonline.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PagoController {

    private final PagoService pagoService;

    // Crear preferencia de pago
    @PostMapping("/preferencia")
    public ResponseEntity<?> crearPreferencia(@RequestBody PreferenciaRequestDTO request) {
        try {
            PreferenciaResponseDTO response = pagoService.crearPreferencia(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // Webhook que llama Mercado Pago al completar el pago
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestParam(required = false) String type,
                                     @RequestParam(required = false) String id) {
        if ("payment".equals(type) && id != null) {
            pagoService.procesarWebhook(id);
        }
        return ResponseEntity.ok().build();
    }
}


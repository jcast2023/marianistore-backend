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

    @PostMapping("/preferencia")
    public ResponseEntity<?> crearPreferencia(@RequestBody PreferenciaRequestDTO request) {
        try {
            PreferenciaResponseDTO response = pagoService.crearPreferencia(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("mensaje", "Error interno al crear preferencia"));
        }
    }

    /**
     * Webhook oficial de Mercado Pago
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) Map<String, Object> body) {

        System.out.println("=== WEBHOOK RECIBIDO ===");
        System.out.println("type: " + type + " | id: " + id);
        System.out.println("Body completo: " + body);

        String paymentId = id;

        // Manejar merchant_order (lo que te está llegando)
        if ("merchant_order".equals(type) && id != null) {
            System.out.println("📦 Detectado merchant_order → Procesando ID: " + id);
            pagoService.procesarWebhook(id);
        }
        // Manejar payment normal
        else if ("payment".equals(type) && id != null) {
            System.out.println("💰 Detectado payment → Procesando ID: " + id);
            pagoService.procesarWebhook(id);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para pruebas (puedes acceder desde el navegador)
     */
    @GetMapping("/webhook")
    public ResponseEntity<?> verificarWebhook() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mensaje", "El endpoint del webhook está activo y escuchando"
        ));
    }
}
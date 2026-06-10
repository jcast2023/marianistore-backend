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

        String topicFinal = type;
        String idFinal = id;

// Si los parámetros de la URL son nulos, buscamos dentro del Body (Estructura de producción)
        if (body != null) {
// 1. Corregido: Usar String.valueOf en lugar de cast directo (String) para el topic
            if (body.containsKey("topic") && topicFinal == null) {
                topicFinal = String.valueOf(body.get("topic"));
            }

// 2. Extraer ID del resource de manera segura
            if (body.containsKey("resource") && idFinal == null) {
                String resource = String.valueOf(body.get("resource"));
                if (resource != null && resource.contains("/")) {
                    idFinal = resource.substring(resource.lastIndexOf("/") + 1);
                }
            }

// 3. Estructura alternativa para webhooks modernos V2 de tipo 'payment'
            if (body.containsKey("action") && "payment.created".equals(String.valueOf(body.get("action")))) {
                topicFinal = "payment";
                if (body.containsKey("data") && body.get("data") instanceof Map) {
                    Map<?, ?> data = (Map<?, ?>) body.get("data");
                    if (data.containsKey("id")) {
                        idFinal = String.valueOf(data.get("id"));
                    }
                }
            }
        }

        System.out.println("🔍 Procesando final -> Topic: " + topicFinal + " | ID: " + idFinal);

// Validamos que el ID no sea "null" como texto producto del String.valueOf()
        if (idFinal != null && !idFinal.isEmpty() && !"null".equalsIgnoreCase(idFinal)
                && ("merchant_order".equals(topicFinal) || "payment".equals(topicFinal))) {
            try {
                pagoService.procesarWebhook(idFinal);
            } catch (Exception e) {
                System.err.println("Error ejecutando el servicio de pago: " + e.getMessage());
// Importante: Devolvemos 200 para que MP no reintente infinitamente si es un error de código
            }
        } else {
            System.out.println("⚠️ Notificación ignorada o formato no soportado.");
        }

// Siempre responder 200 OK a Mercado Pago rápido para evitar timeouts
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
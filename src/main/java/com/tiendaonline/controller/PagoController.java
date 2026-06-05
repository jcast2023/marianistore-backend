package com.tiendaonline.controller;

import com.tiendaonline.dto.PreferenciaRequestDTO;
import com.tiendaonline.dto.PreferenciaResponseDTO;
import com.tiendaonline.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) Map<String, Object> body) {

        System.out.println("=== WEBHOOK RECIBIDO ===");
        System.out.println("type param: " + type);
        System.out.println("id param: " + id);
        System.out.println("body: " + body);

        // Extraer type e id del body si vienen en JSON
        if (body != null) {
            if (type == null) {
                type = (String) body.get("type");
            }
            if (id == null) {
                Object data = body.get("data");
                if (data instanceof Map) {
                    Object dataId = ((Map<?, ?>) data).get("id");
                    if (dataId != null) {
                        id = String.valueOf(dataId);
                    }
                }
            }
        }

        System.out.println("type final: " + type);
        System.out.println("id final: " + id);

        if ("payment".equals(type) && id != null) {
            System.out.println("✅ Procesando pago ID: " + id);
            pagoService.procesarWebhook(id);
        } else {
            System.out.println("⚠️ No se procesó: type=" + type + " id=" + id);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/webhook")
    public ResponseEntity<?> verificarWebhook() {
        System.out.println("=== VERIFICACIÓN DE WEBHOOK DESDE EL NAVEGADOR (GET) ===");
        return ResponseEntity.ok(Map.of("status", "ok", "mensaje", "El endpoint del webhook está escuchando correctamente"));
    }
}
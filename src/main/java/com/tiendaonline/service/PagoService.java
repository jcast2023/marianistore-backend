package com.tiendaonline.service;

import com.tiendaonline.dto.PreferenciaRequestDTO;
import com.tiendaonline.dto.PreferenciaResponseDTO;

import java.math.BigDecimal;

public interface PagoService {
    PreferenciaResponseDTO crearPreferencia(PreferenciaRequestDTO request);
    void procesarWebhook(String paymentId);
    void procesarWebhookConDatos(String paymentId, String externalReference,
                                 BigDecimal monto, String email);
}
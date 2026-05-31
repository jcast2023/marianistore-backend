package com.tiendaonline.service;


import com.tiendaonline.dto.PreferenciaRequestDTO;
import com.tiendaonline.dto.PreferenciaResponseDTO;

public interface PagoService {
    PreferenciaResponseDTO crearPreferencia(PreferenciaRequestDTO request);
    void procesarWebhook(String paymentId);
}

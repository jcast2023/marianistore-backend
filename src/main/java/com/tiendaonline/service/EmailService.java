package com.tiendaonline.service;

import com.tiendaonline.model.Reclamo;

public interface EmailService {
    void enviarCorreoConfirmacion(String emailDestino, String nombreCliente, Object idPedido, Double totalPedido);
    void enviarCorreoReclamo(Reclamo reclamo);
}

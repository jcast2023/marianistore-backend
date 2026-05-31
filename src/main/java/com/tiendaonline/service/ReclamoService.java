package com.tiendaonline.service;

import com.tiendaonline.dto.ReclamoDTO;
import com.tiendaonline.model.Reclamo;

import java.util.List;
import java.util.Optional;

public interface ReclamoService {
    // Método principal para la persistencia del reclamo virtual
    Reclamo registrarReclamo(ReclamoDTO reclamoDTO);

    List<Reclamo> listarTodos();
    Optional<Reclamo> buscarPorCodigo(String codigoReclamo);
}

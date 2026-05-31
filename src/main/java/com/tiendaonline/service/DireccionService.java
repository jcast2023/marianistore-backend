package com.tiendaonline.service;

import java.util.List;
import java.util.Optional;

import com.tiendaonline.dto.DireccionDTO;

public interface DireccionService {

	List<DireccionDTO> listarDirecciones();
    Optional<DireccionDTO> obtenerPorId(Integer id);
    List<DireccionDTO> obtenerPorUsuarioId(Integer idUsuario);
    DireccionDTO crearDireccion(DireccionDTO direccionDTO);
    DireccionDTO actualizarDireccion(Integer id, DireccionDTO direccionDTO);
    void eliminarDireccion(Integer id);
}

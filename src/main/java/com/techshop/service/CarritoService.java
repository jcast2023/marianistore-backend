package com.techshop.service;

import java.util.List;
import java.util.Optional;

import com.techshop.dto.CarritoDTO;

public interface CarritoService {

	List<CarritoDTO> listarCarritos();
    Optional<CarritoDTO> obtenerPorId(Integer id);
    CarritoDTO crearCarrito(CarritoDTO carritoDTO);
    CarritoDTO actualizarCarrito(Integer id, CarritoDTO carritoDTO);
    void eliminarCarrito(Integer id);
}

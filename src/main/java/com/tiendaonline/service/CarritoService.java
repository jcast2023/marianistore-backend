package com.tiendaonline.service;

import java.util.List;
import java.util.Optional;

import com.tiendaonline.dto.CarritoDTO;
import com.tiendaonline.dto.ItemcarritoDTO;

public interface CarritoService {
    List<CarritoDTO> listarCarritos();
    Optional<CarritoDTO> obtenerPorId(Integer id);
    CarritoDTO crearCarrito(CarritoDTO dto);
    CarritoDTO actualizarCarrito(Integer id, CarritoDTO dto);
    void eliminarCarrito(Integer id);
    CarritoDTO obtenerOCrearCarritoActivo(Integer idUsuario);
    List<ItemcarritoDTO> obtenerItemsCarrito(Integer idUsuario);
    ItemcarritoDTO agregarItem(Integer idUsuario, Integer idProducto, Integer cantidad);
    ItemcarritoDTO actualizarCantidadItem(Integer idItem, Integer cantidad);
    void eliminarItem(Integer idItem);
    void vaciarCarrito(Integer idUsuario);
    void sincronizarDesdeLocalStorage(Integer idUsuario, List<ItemcarritoDTO> items);
}
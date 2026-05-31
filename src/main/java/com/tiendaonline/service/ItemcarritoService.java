package com.tiendaonline.service;

import java.util.List;
import java.util.Optional;
import com.tiendaonline.dto.ItemcarritoDTO;

public interface ItemcarritoService {

    List<ItemcarritoDTO> listarItemsCarrito();
    Optional<ItemcarritoDTO> obtenerPorId(Integer id);
    List<ItemcarritoDTO> obtenerPorCarritoId(Integer idCarrito);
    ItemcarritoDTO crearItemCarrito(ItemcarritoDTO itemcarritoDTO);
    ItemcarritoDTO actualizarItemCarrito(Integer id, ItemcarritoDTO itemcarritoDTO);
    void eliminarItemCarrito(Integer id);
}

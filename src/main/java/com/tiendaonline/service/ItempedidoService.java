package com.tiendaonline.service;

import java.util.List;
import java.util.Optional;

import com.tiendaonline.dto.ItempedidoDTO;

public interface ItempedidoService {

	List<ItempedidoDTO> listarItemsPedidos();
    Optional<ItempedidoDTO> obtenerPorId(Integer id);
    List<ItempedidoDTO> obtenerPorPedidoId(Integer idPedido);
    ItempedidoDTO crearItemPedido(ItempedidoDTO itempedidoDTO);
    ItempedidoDTO actualizarItemPedido(Integer id, ItempedidoDTO itempedidoDTO);
    void eliminarItemPedido(Integer id);
}

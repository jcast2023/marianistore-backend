package com.techshop.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException; 
import jakarta.servlet.http.HttpServletResponse; 
import com.techshop.dto.PedidoDTO;

public interface PedidoService {
    List<PedidoDTO> listarPedidos();
    List<PedidoDTO> obtenerPedidosPorUsuario(String email);
    List<PedidoDTO> obtenerPedidosPorUsuarioId(Integer idUsuario);
    Optional<PedidoDTO> obtenerPorId(Integer id);
    PedidoDTO crearPedido(PedidoDTO pedidoDTO);
    PedidoDTO actualizarPedido(Integer id, PedidoDTO pedidoDTO);
    void eliminarPedido(Integer id);
    
    PedidoDTO actualizarEstadoManual(Integer id, String nuevoEstado);
    Map<String, Object> obtenerEstadisticasDashboard();
    
    void exportarFacturaPDF(Integer idPedido, HttpServletResponse response) throws IOException;
    PedidoDTO pagarPedido(Integer id, String metodoPago);
}
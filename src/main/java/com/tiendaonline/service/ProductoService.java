package com.tiendaonline.service;

import java.util.List;
import java.util.Optional;

import com.tiendaonline.dto.ProductoDTO;

public interface ProductoService {

	List<ProductoDTO> listarProductos();
    Optional<ProductoDTO> obtenerPorId(Integer id);
    ProductoDTO crearProducto(ProductoDTO productoDTO);
    ProductoDTO actualizarProducto(Integer id, ProductoDTO productoDTO);
    void eliminarProducto(Integer id);
    ProductoDTO actualizarStock(Integer id, Integer nuevaCantidad);
    Long contarTotalProductos();
    Long contarProductosBajoStock();
    List<ProductoDTO> obtenerTop5MasVendidos();
}

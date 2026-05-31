package com.tiendaonline.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class WishlistDTO {
    private Integer idWishlist;
    private Integer idUsuario;
    private Integer idProducto;
    private String  nombreProducto;
    private String  imagenProducto;
    private BigDecimal precioProducto;
    private String  categoriaProducto;
    private int     stockProducto;
    private LocalDateTime fechaAgregado;
}
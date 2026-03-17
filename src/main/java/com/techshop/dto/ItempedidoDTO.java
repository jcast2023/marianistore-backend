package com.techshop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class ItempedidoDTO {

    private Integer idItemPedido;

    
    private Integer idPedido; 

    @NotNull(message = "El id del producto es obligatorio")
    private Integer idProducto;
    
    private String nombreProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario debe ser positivo o cero")
    private BigDecimal precioUnitario;
    
    private String imagen;
}

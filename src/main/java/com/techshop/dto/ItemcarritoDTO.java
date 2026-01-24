package com.techshop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ItemcarritoDTO {

	private Integer idItem;

    @NotNull(message = "El id del carrito es obligatorio")
    private Integer idCarrito;

    @NotNull(message = "El id del producto es obligatorio")
    private Integer idProducto;

    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;
	
}

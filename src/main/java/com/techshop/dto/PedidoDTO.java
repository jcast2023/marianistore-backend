package com.techshop.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PedidoDTO {
    private Integer idPedido;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer idUsuario; 

    private String emailUsuario;
    
    private Date fechaPedido;

    @NotNull(message = "El total es obligatorio")
    private Double total;
    
    private String estado = "PENDIENTE";

    private List<ItempedidoDTO> items = new ArrayList<>();
}

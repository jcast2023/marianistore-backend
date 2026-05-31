package com.tiendaonline.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PedidoDTO {
    private Integer idPedido;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer idUsuario; 

    private String emailUsuario;
    
    private LocalDateTime fechaPedido;

    @NotNull(message = "El total es obligatorio")
    private BigDecimal total;
    
    private String estado = "PENDIENTE";
    
    private String metodoPago;

    private List<ItempedidoDTO> items = new ArrayList<>();
    
    @NotNull(message = "La dirección de envío es obligatoria")
    private Integer idDireccionEnvio;
    
    private DireccionDTO direccionEnvio;
}

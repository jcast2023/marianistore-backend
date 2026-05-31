package com.tiendaonline.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class CarritoDTO {
    private Integer idCarrito;

    @NotNull(message = "El id del usuario es obligatorio")
    private Integer idUsuario;

    @NotNull(message = "La fecha de creación es obligatoria")
    private LocalDateTime fechaCreacion;

    private String estado;
}

package com.tiendaonline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DireccionDTO {

    private Integer idDireccion;

    @NotNull(message = "El id del usuario es obligatorio")
    private Integer idUsuario;

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    @NotBlank(message = "El código postal es obligatorio")
    private String codigoPostal;

    @NotBlank(message = "El país es obligatorio")
    private String pais;
}

package com.tiendaonline.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioPasswordDTO {
    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String nuevaPassword;
}
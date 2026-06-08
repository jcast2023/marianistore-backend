package com.tiendaonline.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PreferenciaRequestDTO {
    private Integer pedidoId;
    private String descripcion;
    private BigDecimal monto;
    private String email;
    private String nombre;
    private String apellido;
    private String identificationType;
    private String identificationNumber;
}
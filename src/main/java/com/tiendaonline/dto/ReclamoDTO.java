package com.tiendaonline.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReclamoDTO {

    // 1. Identificación del Consumidor
    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellidos;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numDocumento;

    @NotBlank(message = "Debe especificar si es menor de edad")
    private String esMenor;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El departamento es obligatorio")
    private String departamento;

    @NotBlank(message = "La provincia es obligatoria")
    private String provincia;

    @NotBlank(message = "El distrito es obligatorio")
    private String distrito;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo no es válido")
    private String email;

    private String telefono; // Opcional

    // 2. Bien Contratado
    @NotBlank(message = "El tipo de bien es obligatorio")
    private String tipoBien;

    @NotNull(message = "El monto reclamado es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private Double montoReclamado;

    @NotBlank(message = "El número de pedido es obligatorio")
    private String pedido;

    @NotBlank(message = "La descripción del bien es obligatoria")
    private String descripcion;

    // 3. Detalle de la Reclamación
    @NotBlank(message = "El tipo de disconformidad (Reclamo/Queja) es obligatorio")
    private String quejaReclamo;

    @NotBlank(message = "El detalle del reclamo es obligatorio")
    private String detalle;

    private String pedidoConsumidor; // Opcional dependiendo del caso
}

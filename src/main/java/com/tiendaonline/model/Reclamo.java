package com.tiendaonline.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "libro_reclamaciones")
@Data
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código único de reclamo generado automáticamente (Ej: REC-2026-001)
    @Column(name = "codigo_reclamo", unique = true, nullable = false) // 👈 Forzar el nombre exacto de la BD
    private String codigoReclamo;

    @Column(name = "fecha_registro", nullable = false) // 👈 Mapeo explícito
    private LocalDateTime fechaRegistro;

    // 1. Identificación del Consumidor
    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private String tipoDocumento;

    @Column(nullable = false)
    private String numDocumento;

    @Column(nullable = false)
    private String esMenor;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String departamento;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String distrito;

    @Column(nullable = false)
    private String email;

    private String telefono;

    // 2. Bien Contratado
    @Column(name = "tipo_bien", nullable = false)
    private String tipoBien;

    @Column(nullable = false)
    private Double montoReclamado;

    @Column(nullable = false)
    private String pedido;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    // 3. Detalle del Reclamo
    @Column(nullable = false)
    private String quejaReclamo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String detalle;

    @Column(columnDefinition = "TEXT")
    private String pedidoConsumidor;

    // Estado del reclamo (PENDIENTE, EN_REVISION, RESUELTO)
    @Column(nullable = false)
    private String estado;
}

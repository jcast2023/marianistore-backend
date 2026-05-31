package com.tiendaonline.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @OneToOne
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(nullable = false)
    private String metodoPago; // TARJETA_CREDITO, PAYPAL, TRANSFERENCIA

    @Column(nullable = false)
    private Integer monto;

    @Column
    private String paymentId; // ID del pago devuelto por Mercado Pago

    @Column
    private String email;

    @Column(nullable = false)
    private String estado; // COMPLETADO, RECHAZADO, PENDIENTE

    @Column(nullable = false)
    private LocalDateTime fechaPago;
}

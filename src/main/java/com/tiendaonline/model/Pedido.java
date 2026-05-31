package com.tiendaonline.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
@Entity
@Table(name="pedidos")
public class Pedido {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;

    @PastOrPresent(message = "La fecha del pedido no puede ser futura")
    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @DecimalMin(value = "0.0", inclusive = true, message = "El total debe ser mayor o igual que cero")
    @NotNull(message = "El total es obligatorio")
    private BigDecimal total;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;
    
    @Column(name = "metodo_pago")
    private String metodoPago;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itempedido> items = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "direccion_envio_id")   // ← relación con Direccion
    private Direccion direccionEnvio;;
}

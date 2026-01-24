package com.techshop.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name="productos")

public class Producto {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @PositiveOrZero(message = "El precio debe ser positivo o cero")
    private double precio;

    @PositiveOrZero(message = "El stock debe ser positivo o cero")
    private int stock;

    private String imagen;
 
    
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @PastOrPresent(message = "La fecha de creación no puede ser futura")
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @JsonIgnore
    @OneToMany(mappedBy = "producto")
    private List<Itemcarrito> items = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "producto")
    private List<Itempedido> itemsPedido = new ArrayList<>();
}

package com.tiendaonline.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @NotBlank @Size(min = 3, max = 50)
    private String nombre;

    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id_usuario"))
    @Column(name = "roles")
    private List<Role> roles = new ArrayList<>();

    // Ciclo de vida: Asigna la fecha automáticamente antes de guardar en la DB
    @PrePersist
    protected void onCreate() {
    	this.fechaCreacion = LocalDateTime.now();
    }

    public enum Role { USER, ADMIN }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
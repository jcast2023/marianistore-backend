package com.techshop.serviceImplement;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.techshop.dto.UsuarioDTO;
import com.techshop.dto.UsuarioRegistroDTO;
import com.techshop.model.Usuario;
import com.techshop.repository.UsuarioRepository;
import com.techshop.service.UsuarioService;

@Service
public class CustomUserDetailsService implements UserDetailsService, UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    
    public CustomUserDetailsService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findOneByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
       

        Collection<? extends GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                authorities);
    }

    @Override
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UsuarioDTO> obtenerPorId(Integer id) {
        return usuarioRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public Optional<UsuarioDTO> obtenerPorEmail(String email) {
        return usuarioRepository.findOneByEmail(email).map(this::mapToDTO);
    }

    @Override
    public UsuarioDTO crearUsuario(UsuarioRegistroDTO usuarioRegistroDTO) {
        if (usuarioRepository.existsByEmail(usuarioRegistroDTO.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }
        if (usuarioRepository.existsByUsername(usuarioRegistroDTO.getUsername())) {
            throw new IllegalArgumentException("El username ya existe, elige otro.");
        }
        Usuario usuario = mapToEntity(usuarioRegistroDTO);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.getRoles().add(Usuario.Role.USER);
        Usuario saved = usuarioRepository.save(usuario);
        return mapToDTO(saved);
    }

    @Override
    public UsuarioDTO actualizarUsuario(Integer id, UsuarioDTO usuarioDTO) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isPresent()) {
            Usuario usuario = opt.get();
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setEmail(usuarioDTO.getEmail());
            Usuario updated = usuarioRepository.save(usuario);
            return mapToDTO(updated);
        }
        return null;
    }

    @Override
    public void eliminarUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }
    
    @Override
    public boolean actualizarPassword(Integer id, String nuevaPassword) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isPresent()) {
            Usuario usuario = opt.get();
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    private UsuarioDTO mapToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setUsername(usuario.getUsername());
        return dto;
    }

    
    
    private Usuario mapToEntity(UsuarioRegistroDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setUsername(dto.getUsername());
        return usuario;
    }
}
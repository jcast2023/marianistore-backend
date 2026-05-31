package com.tiendaonline.service;

import java.util.List;
import java.util.Optional;
import com.tiendaonline.dto.UsuarioRegistroDTO;

import com.tiendaonline.dto.UsuarioDTO;

public interface UsuarioService {
    List<UsuarioDTO> listarUsuarios();
    Optional<UsuarioDTO> obtenerPorId(Integer id);
    Optional<UsuarioDTO> obtenerPorEmail(String email);
    UsuarioDTO crearUsuario(UsuarioRegistroDTO usuarioDTO);
    UsuarioDTO actualizarUsuario(Integer id, UsuarioDTO usuarioDTO);
    void eliminarUsuario(Integer id);
    boolean actualizarPassword(Integer id, String nuevaPassword);
}

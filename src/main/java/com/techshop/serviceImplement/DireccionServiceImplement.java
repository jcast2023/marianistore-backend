package com.techshop.serviceImplement;

import com.techshop.dto.DireccionDTO;
import com.techshop.model.Direccion;
import com.techshop.model.Usuario;
import com.techshop.repository.DireccionRepository;
import com.techshop.repository.UsuarioRepository;
import com.techshop.service.DireccionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DireccionServiceImplement implements DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DireccionServiceImplement(DireccionRepository direccionRepository, UsuarioRepository usuarioRepository) {
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<DireccionDTO> listarDirecciones() {
        return direccionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DireccionDTO> obtenerPorId(Integer id) {
        return direccionRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public List<DireccionDTO> obtenerPorUsuarioId(Integer idUsuario) {
        return direccionRepository.findAll().stream()
                .filter(d -> d.getUsuario() != null && d.getUsuario().getIdUsuario().equals(idUsuario))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DireccionDTO crearDireccion(DireccionDTO dto) {
        Direccion entity = mapToEntity(dto);
        Direccion saved = direccionRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public DireccionDTO actualizarDireccion(Integer id, DireccionDTO dto) {
        Optional<Direccion> opt = direccionRepository.findById(id);
        if (opt.isPresent()) {
            Direccion entity = opt.get();
            entity.setCalle(dto.getCalle());
            entity.setCiudad(dto.getCiudad());
            entity.setEstado(dto.getEstado());
            entity.setCodigoPostal(dto.getCodigoPostal());
            entity.setPais(dto.getPais());
            
            Direccion updated = direccionRepository.save(entity);
            return mapToDTO(updated);
        }
        return null;
    }

    @Override
    public void eliminarDireccion(Integer id) {
        direccionRepository.deleteById(id);
    }

    private DireccionDTO mapToDTO(Direccion entity) {
        DireccionDTO dto = new DireccionDTO();
        dto.setIdDireccion(entity.getIdDireccion());
        if (entity.getUsuario() != null) {
            dto.setIdUsuario(entity.getUsuario().getIdUsuario());
        }
        dto.setCalle(entity.getCalle());
        dto.setCiudad(entity.getCiudad());
        dto.setEstado(entity.getEstado());
        dto.setCodigoPostal(entity.getCodigoPostal());
        dto.setPais(entity.getPais());
        return dto;
    }

    private Direccion mapToEntity(DireccionDTO dto) {
        Direccion entity = new Direccion();
        entity.setIdDireccion(dto.getIdDireccion());
        
        if (dto.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("No existe Usuario con id " + dto.getIdUsuario()));
            entity.setUsuario(usuario);
        }
        entity.setCalle(dto.getCalle());
        entity.setCiudad(dto.getCiudad());
        entity.setEstado(dto.getEstado());
        entity.setCodigoPostal(dto.getCodigoPostal());
        entity.setPais(dto.getPais());
        return entity;
    }

}

package com.techshop.serviceImplement;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.techshop.dto.CarritoDTO;
import com.techshop.model.Carrito;
import com.techshop.model.Usuario;

import com.techshop.repository.CarritoRepository;
import com.techshop.repository.UsuarioRepository;
import com.techshop.service.CarritoService;

@Service
public class CarritoServiceImplement implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;


    public CarritoServiceImplement(CarritoRepository carritoRepository, UsuarioRepository usuarioRepository) {
        this.carritoRepository = carritoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<CarritoDTO> listarCarritos() {
        return carritoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CarritoDTO> obtenerPorId(Integer id) {
        return carritoRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public CarritoDTO crearCarrito(CarritoDTO dto) {
        Carrito entity = new Carrito();
        entity.setFechaCreacion(dto.getFechaCreacion());
        entity.setEstado(dto.getEstado());

        
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id " + dto.getIdUsuario()));

        entity.setUsuario(usuario);

        Carrito saved = carritoRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public CarritoDTO actualizarCarrito(Integer id, CarritoDTO dto) {
        Optional<Carrito> opt = carritoRepository.findById(id);
        if (opt.isPresent()) {
            Carrito existing = opt.get();
            
            existing.setFechaCreacion(dto.getFechaCreacion());
            existing.setEstado(dto.getEstado());
            

            Carrito updated = carritoRepository.save(existing);
            return mapToDTO(updated);
        } else {
            return null; 
        }
    }

    @Override
    public void eliminarCarrito(Integer id) {
        carritoRepository.deleteById(id);
    }

    private CarritoDTO mapToDTO(Carrito carrito) {
        CarritoDTO dto = new CarritoDTO();
        dto.setIdCarrito(carrito.getIdCarrito());
       
        if (carrito.getUsuario() != null) {
            dto.setIdUsuario(carrito.getUsuario().getIdUsuario());
        }
        dto.setFechaCreacion(carrito.getFechaCreacion());
        dto.setEstado(carrito.getEstado());
        return dto;
    }


   
}

package com.techshop.serviceImplement;

import com.techshop.dto.ItemcarritoDTO;
import com.techshop.model.Carrito;
import com.techshop.model.Itemcarrito;
import com.techshop.model.Producto;
import com.techshop.repository.CarritoRepository;
import com.techshop.repository.ItemcarritoRepository;
import com.techshop.repository.ProductoRepository;
import com.techshop.service.ItemcarritoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemcarritoServiceImplement implements ItemcarritoService {

    private final ItemcarritoRepository itemcarritoRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    public ItemcarritoServiceImplement(ItemcarritoRepository itemcarritoRepository,
                                       CarritoRepository carritoRepository,
                                       ProductoRepository productoRepository) {
        this.itemcarritoRepository = itemcarritoRepository;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<ItemcarritoDTO> listarItemsCarrito() {
        return itemcarritoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ItemcarritoDTO> obtenerPorId(Integer id) {
        return itemcarritoRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public List<ItemcarritoDTO> obtenerPorCarritoId(Integer idCarrito) {
        return itemcarritoRepository.findByCarrito_IdCarrito(idCarrito).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ItemcarritoDTO crearItemCarrito(ItemcarritoDTO dto) {
        Itemcarrito entity = mapToEntity(dto);
        Itemcarrito saved = itemcarritoRepository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public ItemcarritoDTO actualizarItemCarrito(Integer id, ItemcarritoDTO dto) {
        Optional<Itemcarrito> opt = itemcarritoRepository.findById(id);
        if (opt.isPresent()) {
            Itemcarrito entity = opt.get();
            entity.setCantidad(dto.getCantidad());
            

            Itemcarrito updated = itemcarritoRepository.save(entity);
            return mapToDTO(updated);
        }
        return null;
    }

    @Override
    public void eliminarItemCarrito(Integer id) {
        itemcarritoRepository.deleteById(id);
    }

    private ItemcarritoDTO mapToDTO(Itemcarrito entity) {
        ItemcarritoDTO dto = new ItemcarritoDTO();
        dto.setIdItem(entity.getIdItem());
        dto.setCantidad(entity.getCantidad());
        if (entity.getCarrito() != null) {
            dto.setIdCarrito(entity.getCarrito().getIdCarrito());
        }
        if (entity.getProducto() != null) {
            dto.setIdProducto(entity.getProducto().getIdProducto());
        }
        return dto;
    }

    
    private Itemcarrito mapToEntity(ItemcarritoDTO dto) {
        Itemcarrito entity = new Itemcarrito();
        entity.setIdItem(dto.getIdItem());
        entity.setCantidad(dto.getCantidad());

        
        if (dto.getIdCarrito() != null) {
            Carrito carrito = carritoRepository.findById(dto.getIdCarrito())
                .orElseThrow(() -> new IllegalArgumentException("No existe Carrito con id " + dto.getIdCarrito()));
            entity.setCarrito(carrito);
        }

       
        if (dto.getIdProducto() != null) {
            Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new IllegalArgumentException("No existe Producto con id " + dto.getIdProducto()));
            entity.setProducto(producto);
        }
        return entity;
    }
}

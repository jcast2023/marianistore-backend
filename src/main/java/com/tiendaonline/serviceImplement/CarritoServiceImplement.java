package com.tiendaonline.serviceImplement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.dto.CarritoDTO;
import com.tiendaonline.dto.ItemcarritoDTO;
import com.tiendaonline.model.Carrito;
import com.tiendaonline.model.Itemcarrito;
import com.tiendaonline.model.Producto;
import com.tiendaonline.model.Usuario;
import com.tiendaonline.repository.CarritoRepository;
import com.tiendaonline.repository.ItemcarritoRepository;
import com.tiendaonline.repository.ProductoRepository;
import com.tiendaonline.repository.UsuarioRepository;
import com.tiendaonline.service.CarritoService;

@Service
public class CarritoServiceImplement implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemcarritoRepository itemcarritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public CarritoServiceImplement(
            CarritoRepository carritoRepository,
            ItemcarritoRepository itemcarritoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.itemcarritoRepository = itemcarritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    // ── CRUD básico ──────────────────────────────────────────────

    @Override
    public List<CarritoDTO> listarCarritos() {
        return carritoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CarritoDTO> obtenerPorId(Integer id) {
        return carritoRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public CarritoDTO crearCarrito(CarritoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + dto.getIdUsuario()));
        Carrito entity = new Carrito();
        entity.setUsuario(usuario);
        entity.setFechaCreacion(dto.getFechaCreacion() != null ? dto.getFechaCreacion() : LocalDateTime.now());
        entity.setEstado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO");
        return mapToDTO(carritoRepository.save(entity));
    }

    @Override
    public CarritoDTO actualizarCarrito(Integer id, CarritoDTO dto) {
        return carritoRepository.findById(id).map(existing -> {
            existing.setEstado(dto.getEstado());
            existing.setFechaCreacion(dto.getFechaCreacion());
            return mapToDTO(carritoRepository.save(existing));
        }).orElse(null);
    }

    @Override
    public void eliminarCarrito(Integer id) {
        carritoRepository.deleteById(id);
    }

    // ── Carrito persistente ──────────────────────────────────────

    @Override
    @Transactional
    public CarritoDTO obtenerOCrearCarritoActivo(Integer idUsuario) {
        return carritoRepository
                .findByUsuario_IdUsuarioAndEstado(idUsuario, "ACTIVO")
                .map(this::mapToDTO)
                .orElseGet(() -> {
                    // No existe carrito activo → crear uno nuevo
                    Usuario usuario = usuarioRepository.findById(idUsuario)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuario(usuario);
                    nuevo.setFechaCreacion(LocalDateTime.now());
                    nuevo.setEstado("ACTIVO");
                    return mapToDTO(carritoRepository.save(nuevo));
                });
    }

    @Override
    public List<ItemcarritoDTO> obtenerItemsCarrito(Integer idUsuario) {
        Carrito carrito = carritoRepository
                .findByUsuario_IdUsuarioAndEstado(idUsuario, "ACTIVO")
                .orElse(null);
        if (carrito == null) return List.of();

        return itemcarritoRepository
                .findByCarrito_IdCarrito(carrito.getIdCarrito())
                .stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemcarritoDTO agregarItem(Integer idUsuario, Integer idProducto, Integer cantidad) {
        // Obtener o crear carrito activo
        Carrito carrito = carritoRepository
                .findByUsuario_IdUsuarioAndEstado(idUsuario, "ACTIVO")
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(idUsuario)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuario(usuario);
                    nuevo.setFechaCreacion(LocalDateTime.now());
                    nuevo.setEstado("ACTIVO");
                    return carritoRepository.save(nuevo);
                });

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + idProducto));

        // Si el producto ya está en el carrito, sumar cantidad
        List<Itemcarrito> items = itemcarritoRepository.findByCarrito_IdCarrito(carrito.getIdCarrito());
        Optional<Itemcarrito> existente = items.stream()
                .filter(i -> i.getProducto().getIdProducto().equals(idProducto))
                .findFirst();

        Itemcarrito item;
        if (existente.isPresent()) {
            item = existente.get();
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            item = new Itemcarrito();
            item.setCarrito(carrito);
            item.setProducto(producto);
            item.setCantidad(cantidad);
        }

        return mapItemToDTO(itemcarritoRepository.save(item));
    }

    @Override
    @Transactional
    public ItemcarritoDTO actualizarCantidadItem(Integer idItem, Integer cantidad) {
        Itemcarrito item = itemcarritoRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + idItem));
        item.setCantidad(cantidad);
        return mapItemToDTO(itemcarritoRepository.save(item));
    }

    @Override
    @Transactional
    public void eliminarItem(Integer idItem) {
        itemcarritoRepository.deleteById(idItem);
    }

    @Override
    @Transactional
    public void vaciarCarrito(Integer idUsuario) {
        carritoRepository.findByUsuario_IdUsuarioAndEstado(idUsuario, "ACTIVO")
                .ifPresent(carrito -> {
                    List<Itemcarrito> items = itemcarritoRepository
                            .findByCarrito_IdCarrito(carrito.getIdCarrito());
                    itemcarritoRepository.deleteAll(items);
                });
    }

    @Override
    @Transactional
    public void sincronizarDesdeLocalStorage(Integer idUsuario, List<ItemcarritoDTO> itemsLocales) {
        // Vaciar carrito actual en BD
        vaciarCarrito(idUsuario);

        // Insertar todos los items del localStorage
        for (ItemcarritoDTO itemDTO : itemsLocales) {
            agregarItem(idUsuario, itemDTO.getIdProducto(), itemDTO.getCantidad());
        }
    }

    // ── Mappers ──────────────────────────────────────────────────

    private CarritoDTO mapToDTO(Carrito carrito) {
        CarritoDTO dto = new CarritoDTO();
        dto.setIdCarrito(carrito.getIdCarrito());
        if (carrito.getUsuario() != null)
            dto.setIdUsuario(carrito.getUsuario().getIdUsuario());
        dto.setFechaCreacion(carrito.getFechaCreacion());
        dto.setEstado(carrito.getEstado());
        return dto;
    }

    private ItemcarritoDTO mapItemToDTO(Itemcarrito item) {
        ItemcarritoDTO dto = new ItemcarritoDTO();
        dto.setIdItem(item.getIdItem());
        dto.setIdCarrito(item.getCarrito().getIdCarrito());
        dto.setIdProducto(item.getProducto().getIdProducto());
        dto.setCantidad(item.getCantidad());
        return dto;
    }
}
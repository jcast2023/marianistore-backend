package com.tiendaonline.serviceImplement;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tiendaonline.dto.ItempedidoDTO;
import com.tiendaonline.model.Itempedido;
import com.tiendaonline.model.Pedido;
import com.tiendaonline.repository.ItempedidoRepository;
import com.tiendaonline.service.ItempedidoService;
import com.tiendaonline.model.Producto;



@Service
public class ItempedidoServiceImplement implements ItempedidoService {

    private final ItempedidoRepository itempedidoRepository;

    public ItempedidoServiceImplement(ItempedidoRepository itempedidoRepository) {
        this.itempedidoRepository = itempedidoRepository;
    }

    @Override
    public List<ItempedidoDTO> listarItemsPedidos() {
        return itempedidoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ItempedidoDTO> obtenerPorId(Integer id) {
        return itempedidoRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public List<ItempedidoDTO> obtenerPorPedidoId(Integer idPedido) {
        return itempedidoRepository.findByPedidoIdPedido(idPedido).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ItempedidoDTO crearItemPedido(ItempedidoDTO dto) {
        Itempedido itempedido = mapToEntity(dto);
        Itempedido saved = itempedidoRepository.save(itempedido);
        return mapToDTO(saved);
    }

    @Override
    public ItempedidoDTO actualizarItemPedido(Integer id, ItempedidoDTO dto) {
        Optional<Itempedido> optionalEntity = itempedidoRepository.findById(id);
        if (optionalEntity.isPresent()) {
            Itempedido entity = optionalEntity.get();
           
            entity.setCantidad(dto.getCantidad());
            
            Itempedido updated = itempedidoRepository.save(entity);
            return mapToDTO(updated);
        } else {
            return null; 
        }
    }

    @Override
    public void eliminarItemPedido(Integer id) {
        itempedidoRepository.deleteById(id);
    }

    
    private ItempedidoDTO mapToDTO(Itempedido entity) {
        ItempedidoDTO dto = new ItempedidoDTO();
        dto.setIdItemPedido(entity.getIdItemPedido());
        dto.setCantidad(entity.getCantidad());
        dto.setIdPedido(entity.getPedido().getIdPedido());
        dto.setIdProducto(entity.getProducto().getIdProducto());
        
        return dto;
    }

    private Itempedido mapToEntity(ItempedidoDTO dto) {
        Itempedido entity = new Itempedido();
        entity.setIdItemPedido(dto.getIdItemPedido());
        entity.setCantidad(dto.getCantidad());
        entity.setPrecioUnitario(dto.getPrecioUnitario());

       
        Pedido pedido = new Pedido();
        pedido.setIdPedido(dto.getIdPedido());
        entity.setPedido(pedido);

        Producto producto = new Producto();
        producto.setIdProducto(dto.getIdProducto());
        entity.setProducto(producto);

       
        return entity;
    }
}

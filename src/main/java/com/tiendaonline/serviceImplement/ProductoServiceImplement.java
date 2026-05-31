package com.tiendaonline.serviceImplement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiendaonline.dto.ProductoDTO;
import com.tiendaonline.model.Producto;
import com.tiendaonline.repository.ProductoRepository;
import com.tiendaonline.service.ProductoService;

@Service
public class ProductoServiceImplement implements ProductoService {

	@Autowired
	private ProductoRepository productoRepository;
	
	@Override
	public List<ProductoDTO> listarProductos() {
	    return productoRepository.findAll().stream()
	            .map(this::mapToDTO) 
	            .collect(Collectors.toList());
	}

	 @Override
	    public Optional<ProductoDTO> obtenerPorId(Integer id) {
	        return productoRepository.findById(id)
	            .map(this::mapToDTO);
	    }

	    @Override
	    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
	        Producto producto = mapToEntity(productoDTO);
	        producto.setFechaCreacion(LocalDateTime.now());
	        Producto saved = productoRepository.save(producto);
	        return mapToDTO(saved);
	    }

	@Override
	public ProductoDTO actualizarProducto(Integer id, ProductoDTO productoDTO) {
		Optional<Producto> optionalProducto = productoRepository.findById(id);
		if (optionalProducto.isPresent()) {
			Producto existing = optionalProducto.get();
			existing.setNombre(productoDTO.getNombre());
			existing.setDescripcion(productoDTO.getDescripcion());
			existing.setPrecio(productoDTO.getPrecio());
			existing.setImagen(productoDTO.getImagen());

			// ¡LÍNEA AÑADIDA! Setea la segunda imagen al actualizar
			existing.setImagenHover(productoDTO.getImagenHover());

			Producto updated = productoRepository.save(existing);
			return mapToDTO(updated);
		} else {
			return null;
		}
	}

	    @Override
	    public void eliminarProducto(Integer id) {
	        productoRepository.deleteById(id);
	    }
	private ProductoDTO mapToDTO(Producto producto) {
		ProductoDTO dto = new ProductoDTO();
		dto.setIdProducto(producto.getIdProducto());
		dto.setNombre(producto.getNombre());
		dto.setDescripcion(producto.getDescripcion());
		dto.setPrecio(producto.getPrecio());
		dto.setImagen(producto.getImagen());
		dto.setImagenHover(producto.getImagenHover());

		dto.setStock(producto.getStock());
		dto.setCategoria(producto.getCategoria());
		return dto;
	}

	private Producto mapToEntity(ProductoDTO dto) {
		Producto producto = new Producto();
		producto.setIdProducto(dto.getIdProducto());
		producto.setNombre(dto.getNombre());
		producto.setDescripcion(dto.getDescripcion());
		producto.setPrecio(dto.getPrecio());
		producto.setImagen(dto.getImagen());
		producto.setImagenHover(dto.getImagenHover());

		producto.setStock(dto.getStock());

		if (dto.getCategoria() != null) {
			producto.setCategoria(dto.getCategoria());
		}

		return producto;
	}
	    
	    @Override
	    @Transactional 
	    public ProductoDTO actualizarStock(Integer id, Integer nuevaCantidad) {
	        Producto producto = productoRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
	            
	        producto.setStock(nuevaCantidad);
	        return mapToDTO(productoRepository.save(producto));
	    }
	    
	    @Override
	    public Long contarTotalProductos() {
	        return productoRepository.count(); 
	    }

	    @Override
	    public Long contarProductosBajoStock() {
	        
	        return productoRepository.countByStockLessThan(5); 
	    }
	    
	    @Override
	    public List<ProductoDTO> obtenerTop5MasVendidos() {
	      
	        Pageable topFive = PageRequest.of(0, 5);
	        
	       
	        List<Object[]> resultados = productoRepository.findTop5BestSellers(topFive);

	        return resultados.stream().map(obj -> {
	            Producto p = (Producto) obj[0];
	           
	            Long total = (Long) obj[1];
	            
	            ProductoDTO dto = mapToDTO(p);
	          
	            dto.setCantidadVendida(total);
	            
	            return dto;
	        }).collect(Collectors.toList());
	    }
	}

package com.techshop.serviceImplement;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.techshop.dto.PedidoDTO;
import com.techshop.dto.ItempedidoDTO;
import com.techshop.model.Itempedido;
import com.techshop.model.Pedido;
import com.techshop.model.Producto;
import com.techshop.model.Usuario;
import com.techshop.repository.PedidoRepository;
import com.techshop.repository.ProductoRepository;
import com.techshop.repository.UsuarioRepository;
import com.techshop.service.PedidoService;
import com.techshop.service.ProductoService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImplement implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoService productoService;

    public PedidoServiceImplement(PedidoRepository pedidoRepository, 
                                 ProductoRepository productoRepository,
                                 UsuarioRepository usuarioRepository,
                                 ProductoService productoService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoService = productoService;
    }

   

    @Override
    public void exportarFacturaPDF(Integer idPedido, HttpServletResponse response) throws IOException {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Color azulTech = new Color(0, 123, 255);
        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, azulTech);
        Font fuenteCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

       
        try {
            String urlPedido = "http://localhost:4200/mis-pedidos/" + pedido.getIdPedido();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(urlPedido, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            Image qrImage = Image.getInstance(pngOutputStream.toByteArray());
            qrImage.setAlignment(Element.ALIGN_RIGHT);
            qrImage.scalePercent(50);
            document.add(qrImage);
        } catch (Exception e) { System.err.println("Error QR: " + e.getMessage()); }

        document.add(new Paragraph("TechShop – Tu tienda de tecnología", fuenteTitulo));
        document.add(new Paragraph("Los mejores productos al mejor precio", FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY)));
        document.add(new Chunk("\n"));

       
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.addCell(getStyledCell("DETALLES DEL CLIENTE", fuenteCabecera, Color.DARK_GRAY));
        infoTable.addCell(getStyledCell("DETALLES DEL PEDIDO", fuenteCabecera, Color.DARK_GRAY));
        infoTable.addCell(new Phrase("Nombre: " + pedido.getUsuario().getNombre() + "\nEmail: " + pedido.getUsuario().getEmail(), fuenteNormal));
        infoTable.addCell(new Phrase("Factura #: " + pedido.getIdPedido() + "\nFecha: " + pedido.getFechaPedido() + "\nEstado: " + pedido.getEstado(), fuenteNormal));
        document.add(infoTable);

       
        PdfPTable tablaProductos = new PdfPTable(4);
        tablaProductos.setWidthPercentage(100);
        tablaProductos.setSpacingBefore(20);
        tablaProductos.setWidths(new float[]{45, 10, 20, 25});

        String[] headers = {"Producto", "Cant.", "P. Unitario", "Subtotal"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fuenteCabecera));
            cell.setBackgroundColor(azulTech);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            tablaProductos.addCell(cell);
        }

        for (Itempedido item : pedido.getItems()) {
            tablaProductos.addCell(new Phrase(item.getProducto().getNombre(), fuenteNormal));
            tablaProductos.addCell(new Phrase(String.valueOf(item.getCantidad()), fuenteNormal));
            tablaProductos.addCell(new Phrase("$" + String.format("%.2f", item.getPrecioUnitario()), fuenteNormal));
            tablaProductos.addCell(new Phrase("$" + String.format("%.2f", item.getCantidad() * item.getPrecioUnitario()), fuenteNormal));
        }
        document.add(tablaProductos);

        Paragraph total = new Paragraph("\nTOTAL PAGADO: $" + String.format("%.2f", pedido.getTotal()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, azulTech));
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

       
        try {
            document.add(new Chunk("\n\n"));
            String codigoBarrasStr = String.format("%012d", pedido.getIdPedido());
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix barcodeMatrix = barcodeWriter.encode(codigoBarrasStr, BarcodeFormat.CODE_128, 300, 50);
            ByteArrayOutputStream barcodeStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(barcodeMatrix, "PNG", barcodeStream);
            Image barcodeImage = Image.getInstance(barcodeStream.toByteArray());
            barcodeImage.setAlignment(Element.ALIGN_CENTER);
            document.add(barcodeImage);
        } catch (Exception e) { System.err.println("Error Barras: " + e.getMessage()); }

        document.close();
    }

    private PdfPCell getStyledCell(String texto, Font fuente, Color color) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        cell.setBackgroundColor(color);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    

    @Override
    public List<PedidoDTO> listarPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoDTO> obtenerPedidosPorUsuario(String email) {
        return pedidoRepository.findByUsuarioEmail(email).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PedidoDTO> obtenerPorId(Integer id) {
        return pedidoRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public PedidoDTO crearPedido(PedidoDTO pedidoDTO) {
       
        if (pedidoDTO.getIdUsuario() == null) {
            throw new IllegalArgumentException("El ID de usuario es obligatorio para crear el pedido.");
        }

        Pedido pedido = new Pedido();
        
      
        Usuario usuario = usuarioRepository.findById(pedidoDTO.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + pedidoDTO.getIdUsuario()));
        
        pedido.setUsuario(usuario);
        pedido.setFechaPedido(new java.util.Date());
        pedido.setEstado("PENDIENTE");
        pedido.setTotal(pedidoDTO.getTotal());

        
        List<Itempedido> items = pedidoDTO.getItems().stream().map(itemDTO -> {
            if (itemDTO.getIdProducto() == null) {
                throw new IllegalArgumentException("El ID del producto en el ítem no puede ser nulo.");
            }

            Producto producto = productoRepository.findById(itemDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + itemDTO.getIdProducto()));
            
            if (producto.getStock() < itemDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

           
            producto.setStock(producto.getStock() - itemDTO.getCantidad());
            productoRepository.save(producto);

            Itempedido item = new Itempedido();
            item.setPedido(pedido);
            item.setProducto(producto);
            item.setCantidad(itemDTO.getCantidad());
            item.setPrecioUnitario(itemDTO.getPrecioUnitario());
            return item;
        }).collect(Collectors.toList());

        pedido.setItems(items);
        Pedido guardado = pedidoRepository.save(pedido);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public PedidoDTO pagarPedido(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstado("PAGADO");
        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoDTO actualizarPedido(Integer id, PedidoDTO pedidoDTO) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstado(pedidoDTO.getEstado());
        return mapToDTO(pedidoRepository.save(pedido));
    }

    @Override
    public void eliminarPedido(Integer id) {
        pedidoRepository.deleteById(id);
    }

  
    private PedidoDTO mapToDTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setIdUsuario(pedido.getUsuario().getIdUsuario());
        dto.setEmailUsuario(pedido.getUsuario().getEmail());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setEstado(pedido.getEstado());
        dto.setTotal(pedido.getTotal());

        
        List<ItempedidoDTO> itemDTOs = pedido.getItems().stream().map(item -> {
            ItempedidoDTO itemDTO = new ItempedidoDTO();
            itemDTO.setIdItemPedido(item.getIdItemPedido());
            itemDTO.setIdPedido(pedido.getIdPedido());
            itemDTO.setIdProducto(item.getProducto().getIdProducto());
            itemDTO.setNombreProducto(item.getProducto().getNombre()); 
            itemDTO.setCantidad(item.getCantidad());
            itemDTO.setPrecioUnitario(item.getPrecioUnitario());
            itemDTO.setImagen(item.getProducto().getImagen()); 
            return itemDTO;
        }).collect(Collectors.toList());

        dto.setItems(itemDTOs); 
        return dto;
    }
    
    @Override 
    public PedidoDTO actualizarEstadoManual(Integer id, String nuevoEstado) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setEstado(nuevoEstado); 
            Pedido pedidoGuardado = pedidoRepository.save(pedido);
            return mapToDTO(pedidoGuardado); 
        }).orElse(null);
    }
    

    public Map<String, Object> obtenerEstadisticasDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("ventasTotales", pedidoRepository.sumarVentasTotales());
        stats.put("pedidosPendientes", pedidoRepository.contarPedidosPorEnviar());
        stats.put("stockCritico", productoService.contarProductosBajoStock()); 
        stats.put("totalProductos", productoRepository.count());
        return stats;
    }
}
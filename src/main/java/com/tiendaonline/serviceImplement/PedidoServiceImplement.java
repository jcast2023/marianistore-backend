package com.tiendaonline.serviceImplement;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.tiendaonline.dto.PedidoDTO;
import com.tiendaonline.dto.DireccionDTO;
import com.tiendaonline.dto.ItempedidoDTO;
import com.tiendaonline.model.Direccion;
import com.tiendaonline.model.Itemcarrito;
import com.tiendaonline.model.Itempedido;
import com.tiendaonline.model.Pago;
import com.tiendaonline.model.Pedido;
import com.tiendaonline.model.Producto;
import com.tiendaonline.model.Usuario;
import com.tiendaonline.repository.CarritoRepository;
import com.tiendaonline.repository.DireccionRepository;
import com.tiendaonline.repository.ItemcarritoRepository;
import com.tiendaonline.repository.PagoRepository;
import com.tiendaonline.repository.PedidoRepository;
import com.tiendaonline.repository.ProductoRepository;
import com.tiendaonline.repository.UsuarioRepository;
import com.tiendaonline.service.EmailService;
import com.tiendaonline.service.PedidoService;
import com.tiendaonline.service.ProductoService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final DireccionRepository direccionRepository;
    private final CarritoRepository carritoRepository;
    private final ItemcarritoRepository itemcarritoRepository;
    private final EmailService emailService;
    private final PagoRepository pagoRepository;

    public PedidoServiceImplement(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            ProductoService productoService,
            DireccionRepository direccionRepository,
            CarritoRepository carritoRepository,
            ItemcarritoRepository itemcarritoRepository,
            EmailService emailService,
            PagoRepository pagoRepository) {
        this.pedidoRepository      = pedidoRepository;
        this.productoRepository    = productoRepository;
        this.usuarioRepository     = usuarioRepository;
        this.productoService       = productoService;
        this.direccionRepository   = direccionRepository;
        this.carritoRepository     = carritoRepository;
        this.itemcarritoRepository = itemcarritoRepository;
        this.emailService          = emailService;
        this.pagoRepository        = pagoRepository;
    }

    @Override
    @Transactional
    public PedidoDTO crearPedido(PedidoDTO pedidoDTO) {

        if (pedidoDTO.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El ID de usuario es obligatorio.");
        }

        Pedido pedido = new Pedido();

        Usuario usuario =
                usuarioRepository.findById(
                                pedidoDTO.getIdUsuario())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Usuario no encontrado con ID: "
                                                + pedidoDTO.getIdUsuario()));

        pedido.setUsuario(usuario);

        pedido.setFechaPedido(
                LocalDateTime.now());

        pedido.setEstado(
                "PENDIENTE");


    /*
      VALIDAR DIRECCION
    */

        if (pedidoDTO.getIdDireccionEnvio() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una dirección");
        }

        Direccion direccion =
                direccionRepository.findById(
                                pedidoDTO.getIdDireccionEnvio())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Dirección no encontrada"));

        if (!direccion.getUsuario()
                .getIdUsuario()
                .equals(usuario.getIdUsuario())) {

            throw new IllegalArgumentException(
                    "La dirección no pertenece al usuario");
        }

        pedido.setDireccionEnvio(
                direccion);


    /*
   CREAR ITEMS DEL PEDIDO
*/

        if (
                pedidoDTO.getItems() == null ||
                        pedidoDTO.getItems().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "El pedido debe contener items"
            );

        }

        List<Itempedido> items =

                pedidoDTO.getItems()

                        .stream()

                        .map(itemDTO -> {

                            if(itemDTO.getIdProducto()==null){

                                throw new IllegalArgumentException(
                                        "Producto inválido"
                                );

                            }

                            Producto producto =

                                    productoRepository.findById(
                                                    itemDTO.getIdProducto()
                                            )

                                            .orElseThrow(
                                                    ()->new RuntimeException(
                                                            "Producto no encontrado"
                                                    )
                                            );

                    /*
                      VALIDAR STOCK
                    */

                            if(
                                    producto.getStock()
                                            <
                                            itemDTO.getCantidad()
                            ){

                                throw new RuntimeException(

                                        "Stock insuficiente para "

                                                + producto.getNombre()

                                );

                            }

                    /*
                       DESCONTAR STOCK
                    */

                            producto.setStock(

                                    producto.getStock()

                                            -

                                            itemDTO.getCantidad()

                            );

                            productoRepository.save(
                                    producto
                            );

                            Itempedido item =
                                    new Itempedido();

                            item.setPedido(
                                    pedido
                            );

                            item.setProducto(
                                    producto
                            );

                            item.setCantidad(
                                    itemDTO.getCantidad()
                            );

                    /*
                       PRECIO REAL DE BD
                    */

                            item.setPrecioUnitario(
                                    producto.getPrecio()
                            );

                            return item;

                        })

                        .collect(Collectors.toList());



/*
   CALCULAR TOTAL REAL
*/

        BigDecimal totalCalculado =

                items.stream()

                        .map(

                                item ->

                                        item.getPrecioUnitario()

                                                .multiply(

                                                        BigDecimal.valueOf(

                                                                item.getCantidad()

                                                        )

                                                )

                        )

                        .reduce(

                                BigDecimal.ZERO,

                                BigDecimal::add

                        );


        pedido.setTotal(
                totalCalculado
        );

        pedido.setItems(
                items
        );


    /*
      GUARDAR PEDIDO
    */

        Pedido guardado =
                pedidoRepository.save(
                        pedido);



    /*
      VACIAR CARRITO BD
    */

        carritoRepository
                .findByUsuario_IdUsuarioAndEstado(
                        usuario.getIdUsuario(),
                        "ACTIVO")

                .ifPresent(carrito -> {

                    List<Itemcarrito> itemsCarrito =
                            itemcarritoRepository
                                    .findByCarrito_IdCarrito(
                                            carrito.getIdCarrito());

                    itemcarritoRepository
                            .deleteAll(
                                    itemsCarrito);

                });


        return mapToDTO(
                guardado);
    }

    @Override
    @Transactional
    public PedidoDTO pagarPedido(Integer id, String metodoPago) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if ("PAGADO".equals(pedido.getEstado())) {

            System.out.println(
                    "Pedido "
                            + pedido.getIdPedido()
                            + " ya estaba pagado"
            );

            return mapToDTO(pedido);

        }

        pedido.setEstado("PAGADO");
        if (metodoPago != null && !metodoPago.isEmpty()) {
            pedido.setMetodoPago(metodoPago);
        }
        pedidoRepository.save(pedido);

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(metodoPago != null ? metodoPago : "DESCONOCIDO");
        BigDecimal totalSeguro =

                pedido.getTotal()==null

                        ?

                        BigDecimal.ZERO

                        :

                        pedido.getTotal();


        pago.setMonto(

                totalSeguro

                        .multiply(
                                BigDecimal.valueOf(100)
                        )

                        .setScale(
                                0,
                                java.math.RoundingMode.HALF_UP
                        )

                        .intValue()

        );
        pago.setEstado("COMPLETADO");
        pago.setEmail(pedido.getUsuario().getEmail());
        pago.setFechaPago(LocalDateTime.now());
        pagoRepository.save(pago);

        // ✅ CORREO AQUÍ — después de confirmar el pago
        try {
            Double totalDouble = pedido.getTotal() != null ? pedido.getTotal().doubleValue() : 0.0;
            emailService.enviarCorreoConfirmacion(
                    pedido.getUsuario().getEmail(),
                    pedido.getUsuario().getNombre(),
                    pedido.getIdPedido(),
                    totalDouble
            );
        } catch (Exception e) {
            System.err.println("⚠️ Pago registrado, pero falló el envío de correo: " + e.getMessage());
        }

        return mapToDTO(pedido);
    }

    @Override
    public List<PedidoDTO> listarPedidos() {
        return pedidoRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<PedidoDTO> obtenerPedidosPorUsuario(String email) {
        return pedidoRepository.findByUsuarioEmail(email).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<PedidoDTO> obtenerPorId(Integer id) {
        return pedidoRepository.findById(id).map(this::mapToDTO);
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

    @Override
    public PedidoDTO actualizarEstadoManual(Integer id, String nuevoEstado) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setEstado(nuevoEstado);
            return mapToDTO(pedidoRepository.save(pedido));
        }).orElse(null);
    }

    @Override
    public List<PedidoDTO> obtenerPedidosPorUsuarioId(Integer idUsuario) {
        return pedidoRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> obtenerEstadisticasDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("ventasTotales",     pedidoRepository.sumarVentasTotales());
        stats.put("pedidosPendientes", pedidoRepository.contarPedidosPorEnviar());
        stats.put("stockCritico",      productoService.contarProductosBajoStock());
        stats.put("totalProductos",    productoRepository.count());
        return stats;
    }

    @Override
    public void exportarFacturaPDF(Integer idPedido, HttpServletResponse response) throws IOException {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Color azulTech      = new Color(0, 123, 255);
        Font fuenteTitulo   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, azulTech);
        Font fuenteCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font fuenteNormal   = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

        try {
            String urlPedido =

                    "https://marianistore-frontend.vercel.app/mis-pedidos";
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
        document.add(new Paragraph("Los mejores productos al mejor precio",
                FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY)));
        document.add(new Chunk("\n"));

        PdfPTable infoTable = new PdfPTable(3);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{33, 33, 34});
        infoTable.addCell(getStyledCell("DETALLES DEL CLIENTE", fuenteCabecera, Color.DARK_GRAY));
        infoTable.addCell(getStyledCell("DETALLES DEL PEDIDO",  fuenteCabecera, Color.DARK_GRAY));
        infoTable.addCell(getStyledCell("DIRECCIÓN DE ENVÍO",   fuenteCabecera, Color.DARK_GRAY));

        infoTable.addCell(new Phrase(
                "Nombre: " + pedido.getUsuario().getNombre() +
                        "\nEmail: "  + pedido.getUsuario().getEmail(), fuenteNormal));

        String datosPedido = "Factura #: " + pedido.getIdPedido() +
                "\nFecha: "  + pedido.getFechaPedido() +
                "\nEstado: " + pedido.getEstado();
        if (pedido.getMetodoPago() != null)
            datosPedido += "\nMétodo: " + obtenerNombreMetodoPago(pedido.getMetodoPago());
        infoTable.addCell(new Phrase(datosPedido, fuenteNormal));

        String direccionEnvio = "Sin dirección registrada";
        if (pedido.getDireccionEnvio() != null) {
            Direccion dir = pedido.getDireccionEnvio();
            direccionEnvio = dir.getCalle() + "\n" + dir.getCiudad() + ", " + dir.getEstado()
                    + "\n" + dir.getPais() + " - " + dir.getCodigoPostal();
        }
        infoTable.addCell(new Phrase(direccionEnvio, fuenteNormal));
        document.add(infoTable);

        PdfPTable tablaProductos = new PdfPTable(4);
        tablaProductos.setWidthPercentage(100);
        tablaProductos.setSpacingBefore(20);
        tablaProductos.setWidths(new float[]{45, 10, 20, 25});

        for (String h : new String[]{"Producto", "Cant.", "P. Unitario", "Subtotal"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fuenteCabecera));
            cell.setBackgroundColor(azulTech);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            tablaProductos.addCell(cell);
        }

        List<Itempedido> itemsFactura =

                pedido.getItems()==null

                        ?

                        List.of()

                        :

                        pedido.getItems();


        for(Itempedido item : itemsFactura){

            tablaProductos.addCell(
                    new Phrase(
                            item.getProducto().getNombre(),
                            fuenteNormal
                    )
            );

            tablaProductos.addCell(
                    new Phrase(
                            String.valueOf(
                                    item.getCantidad()
                            ),
                            fuenteNormal
                    )
            );

            tablaProductos.addCell(

                    new Phrase(

                            "S/" +

                                    String.format(
                                            "%.2f",
                                            item.getPrecioUnitario()
                                    ),

                            fuenteNormal

                    )

            );

            BigDecimal subtotal =

                    item.getPrecioUnitario()

                            .multiply(

                                    BigDecimal.valueOf(
                                            item.getCantidad()
                                    )

                            );

            tablaProductos.addCell(

                    new Phrase(

                            "S/" +

                                    String.format(
                                            "%.2f",
                                            subtotal
                                    ),

                            fuenteNormal

                    )

            );

        }
        document.add(tablaProductos);

        BigDecimal totalSeguro =

                pedido.getTotal()==null

                        ?

                        BigDecimal.ZERO

                        :

                        pedido.getTotal();


        Paragraph total = new Paragraph(

                "\nTOTAL PAGADO: S/" +

                        String.format(
                                "%.2f",
                                totalSeguro
                        ),

                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        16,
                        azulTech
                )

        );

        total.setAlignment(
                Element.ALIGN_RIGHT
        );

        document.add(
                total
        );

        try {
            document.add(new Chunk("\n\n"));
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix barcodeMatrix = barcodeWriter.encode(
                    String.format("%012d", pedido.getIdPedido()), BarcodeFormat.CODE_128, 300, 50);
            ByteArrayOutputStream barcodeStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(barcodeMatrix, "PNG", barcodeStream);
            Image barcodeImage = Image.getInstance(barcodeStream.toByteArray());
            barcodeImage.setAlignment(Element.ALIGN_CENTER);
            document.add(barcodeImage);
        } catch (Exception e) { System.err.println("Error Barras: " + e.getMessage()); }

        document.close();
    }

    private String obtenerNombreMetodoPago(String metodoPago) {
        switch (metodoPago) {
            case "TARJETA_CREDITO": return "Tarjeta de Crédito (Mercado Pago)";
            case "PAYPAL":          return "PayPal";
            case "TRANSFERENCIA":   return "Transferencia Bancaria";
            default:                return metodoPago;
        }
    }

    private PdfPCell getStyledCell(String texto, Font fuente, Color color) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        cell.setBackgroundColor(color);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PedidoDTO mapToDTO(Pedido pedido) {

        PedidoDTO dto =
                new PedidoDTO();

        dto.setIdPedido(
                pedido.getIdPedido()
        );

        dto.setIdUsuario(
                pedido.getUsuario().getIdUsuario()
        );

        dto.setEmailUsuario(
                pedido.getUsuario().getEmail()
        );

        dto.setFechaPedido(
                pedido.getFechaPedido()
        );

        dto.setEstado(
                pedido.getEstado()
        );

        dto.setTotal(
                pedido.getTotal()
        );

        dto.setMetodoPago(
                pedido.getMetodoPago()
        );


        if(
                pedido.getDireccionEnvio()
                        !=
                        null
        ){

            dto.setIdDireccionEnvio(

                    pedido.getDireccionEnvio()

                            .getIdDireccion()

            );

            dto.setDireccionEnvio(

                    mapDireccionToDTO(
                            pedido.getDireccionEnvio()
                    )

            );

        }


        List<ItempedidoDTO> itemDTOs =

                pedido.getItems()==null

                        ?

                        List.of()

                        :

                        pedido.getItems()

                                .stream()

                                .map(item->{

                                    ItempedidoDTO itemDTO =
                                            new ItempedidoDTO();

                                    itemDTO.setIdItemPedido(
                                            item.getIdItemPedido()
                                    );

                                    itemDTO.setIdPedido(
                                            pedido.getIdPedido()
                                    );

                                    itemDTO.setIdProducto(
                                            item.getProducto()
                                                    .getIdProducto()
                                    );

                                    itemDTO.setNombreProducto(
                                            item.getProducto()
                                                    .getNombre()
                                    );

                                    itemDTO.setCantidad(
                                            item.getCantidad()
                                    );

                                    itemDTO.setPrecioUnitario(
                                            item.getPrecioUnitario()
                                    );

                                    itemDTO.setImagen(
                                            item.getProducto()
                                                    .getImagen()
                                    );

                                    return itemDTO;

                                })

                                .collect(
                                        Collectors.toList()
                                );


        dto.setItems(
                itemDTOs
        );

        return dto;

    }

    private DireccionDTO mapDireccionToDTO(Direccion direccion) {
        if (direccion == null) return null;
        DireccionDTO dto = new DireccionDTO();
        dto.setIdDireccion(direccion.getIdDireccion());
        dto.setIdUsuario(direccion.getUsuario().getIdUsuario());
        dto.setCalle(direccion.getCalle());
        dto.setCiudad(direccion.getCiudad());
        dto.setEstado(direccion.getEstado());
        dto.setCodigoPostal(direccion.getCodigoPostal());
        dto.setPais(direccion.getPais());
        return dto;
    }
}
package com.tiendaonline.serviceImplement;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

import com.tiendaonline.dto.PreferenciaRequestDTO;
import com.tiendaonline.dto.PreferenciaResponseDTO;
import com.tiendaonline.model.Pago;
import com.tiendaonline.model.Pedido;
import com.tiendaonline.repository.PagoRepository;
import com.tiendaonline.repository.PedidoRepository;
import com.tiendaonline.service.EmailService;
import com.tiendaonline.service.PagoService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import com.mercadopago.resources.merchantorder.MerchantOrderPayment;


@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    @Value("${mercadopago.access.token}")
    private String mpToken;

    @Value("${mercadopago.domain}")
    private String mpDomain;

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final EmailService emailService;

    @PostConstruct
    public void initMP() {
        MercadoPagoConfig.setAccessToken(mpToken);
    }

    @Override
    public PreferenciaResponseDTO crearPreferencia(PreferenciaRequestDTO request, String emailAutenticado) {
        log.info("====== ENTRANDO A CREAR PREFERENCIA ======");
        log.info("Pedido ID: {}", request.getPedidoId());
        log.info("Email autenticado: {}", emailAutenticado);

        if (request.getPedidoId() == null) {
            throw new IllegalArgumentException("El ID del pedido no puede ser nulo.");
        }

        try {
            Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            // ✅ VALIDACIÓN DE PROPIEDAD: el pedido debe pertenecer al usuario autenticado
            if (emailAutenticado == null || emailAutenticado.isEmpty()) {
                throw new RuntimeException("No se pudo verificar la identidad del usuario");
            }
            if (pedido.getUsuario() == null) {
                throw new RuntimeException("El pedido no tiene usuario asociado");
            }
            boolean esPropietario = emailAutenticado.equalsIgnoreCase(pedido.getUsuario().getEmail());
            if (!esPropietario) {
                log.warn("RECHAZADO: el pedido {} no pertenece al usuario {}",
                        request.getPedidoId(), emailAutenticado);
                throw new RuntimeException("No tienes permiso para pagar un pedido que no es tuyo");
            }

            log.info("Propiedad validada: pedido {} pertenece a {}",
                    request.getPedidoId(), emailAutenticado);

            BigDecimal monto = pedido.getTotal();

            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("El monto del pedido es inválido: " + monto);
            }

            // SOLUCIÓN AL BLOQUEO: Generar una referencia única por cada intento de pago.
            // Ejemplo resultado: "176-1718416800" (ID del pedido + timestamp actual)
            String intentoPagoUnicoId = request.getPedidoId() + "-" + System.currentTimeMillis();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(intentoPagoUnicoId)
                    .title("Compra en MarianiStore")
                    .description("Pedido #" + request.getPedidoId() + " - Artículos varios")
                    .quantity(1)
                    .unitPrice(monto)
                    .currencyId("PEN")
                    .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(request.getEmail())
                    .name(request.getNombre())
                    .surname(request.getApellido() != null ? request.getApellido() : "")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://marianistore-frontend.vercel.app/mis-pedidos?status=success")
                    .failure("https://marianistore-frontend.vercel.app/mis-pedidos?status=failure")
                    .pending("https://marianistore-frontend.vercel.app/mis-pedidos?status=pending")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(Collections.singletonList(item))
                    .payer(payer)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .statementDescriptor("MARIANISTORE")
                    .externalReference(intentoPagoUnicoId)
                    .notificationUrl(mpDomain + "/api/pagos/webhook")
                    .binaryMode(false)
                    .build();

            Preference preference = new PreferenceClient().create(preferenceRequest);

            log.info("=== PREFERENCIA CREADA CORRECTAMENTE ===");
            log.info("Init Point (Producción): {}", preference.getInitPoint());

            PreferenciaResponseDTO response = new PreferenciaResponseDTO();
            response.setPreferenceId(preference.getId());
            response.setInitPoint(preference.getInitPoint());
            response.setSandboxUrl(null);

            return response;

        } catch (Exception e) {
            log.error("Error al crear preferencia", e);
            throw new RuntimeException("Error al crear preferencia: " + e.getMessage(), e);
        }
    }

    @Override
    public void procesarWebhook(String id) {
        log.info("====== PROCESANDO WEBHOOK ======");
        log.info("ID recibido: {}", id);

        MPRequestOptions requestOptions = MPRequestOptions.builder()
                .accessToken(mpToken)
                .build();

        try {
            // Intentar como Payment primero
            try {
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(Long.parseLong(id), requestOptions);

                log.info("Procesando como PAGO. Estado: {}", payment.getStatus());

                // Solo procesar si está aprobado
                if ("approved".equals(payment.getStatus())) {
                    actualizarPedido(
                            String.valueOf(payment.getId()),
                            payment.getExternalReference(),
                            payment.getTransactionAmount(),
                            payment.getPayer() != null ? payment.getPayer().getEmail() : null
                    );
                } else {
                    log.warn("Pago no aprobado. Estado: {} - No se actualiza el pedido", payment.getStatus());
                    return;
                }
                return;

            } catch (MPApiException e) {
                if (e.getStatusCode() != 404) {
                    log.warn("Error API (no 404): {}", e.getMessage());
                    return;
                }
                log.info("El ID no corresponde a un Pago individual (404). Evaluando como MerchantOrder...");
            } catch (NumberFormatException e) {
                log.warn("ID no es numérico: {}", id);
                return;
            }

            // Intentar como MerchantOrder
            try {
                MerchantOrderClient orderClient = new MerchantOrderClient();
                MerchantOrder order = orderClient.get(Long.parseLong(id), requestOptions);
                log.info("Procesando como ORDEN. Estado de la orden: {}", order.getStatus());

                if (order.getPayments() != null && !order.getPayments().isEmpty()) {
                    for (MerchantOrderPayment mop : order.getPayments()) {
                        log.info("  Pago en orden - ID: {}, Estado: {}", mop.getId(), mop.getStatus());

                        if ("approved".equals(mop.getStatus())) {
                            log.info("Pago aprobado encontrado dentro de la orden: {}", mop.getId());
                            actualizarPedido(
                                    String.valueOf(mop.getId()),
                                    order.getExternalReference(),
                                    mop.getTransactionAmount(),
                                    order.getPayer() != null && order.getPayer().getId() != null ?
                                            "usuario_mp_" + order.getPayer().getId() + "@test.com" : null
                            );
                            return;
                        }
                    }
                    log.warn("Ningún pago aprobado en la orden.");
                } else {
                    log.warn("La orden no tiene pagos asociados.");
                }

            } catch (MPApiException e) {
                log.warn("Error obteniendo orden (MPApiException): {}", e.getMessage());
            } catch (NumberFormatException e) {
                log.warn("ID no es numérico para MerchantOrder: {}", id);
            } catch (Exception e) {
                log.warn("Error procesando orden: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("====== ERROR GENERAL EN WEBHOOK ======");
            log.error("Error: {}", e.getMessage(), e);
            // NO lanzar RuntimeException para evitar que MP reintente innecesariamente
        }
    }

    @Override
    public void procesarWebhookConDatos(String paymentId, String externalReference,
                                        BigDecimal monto, String email) {
        log.info("====== PROCESANDO PAGO DIRECTO DESDE WEBHOOK ======");
        log.info("Payment ID: {}", paymentId);
        log.info("External Reference (Pedido ID): {}", externalReference);
        actualizarPedido(paymentId, externalReference, monto, email);
    }

    private void actualizarPedido(String paymentId, String externalReference,
                                  BigDecimal monto, String email) {
        try {
            if (externalReference == null || externalReference.isEmpty()) {
                throw new IllegalArgumentException("External Reference vacía o nula.");
            }

            // Extraer el ID real del pedido ignorando el timestamp posterior al guión
            String realPedidoIdStr = externalReference.contains("-")
                    ? externalReference.split("-")[0]
                    : externalReference;

            Integer pedidoId = Integer.parseInt(realPedidoIdStr);
            log.info("Actualizando Pedido ID Real: {}", pedidoId);

            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

            // ✅ VALIDACIÓN DE MONTO: el pago debe coincidir con el total del pedido
            if (monto == null) {
                throw new RuntimeException("El monto del pago es nulo, no se puede validar");
            }

            BigDecimal totalPedido = pedido.getTotal();
            if (totalPedido == null) {
                throw new RuntimeException("El pedido no tiene total definido");
            }

            // Tolerancia de 1 centavo para evitar diferencias por redondeo
            BigDecimal diferencia = monto.subtract(totalPedido).abs();
            if (diferencia.compareTo(new BigDecimal("0.01")) > 0) {
                log.warn("RECHAZADO: monto del pago ({}) no coincide con total del pedido ({})",
                        monto, totalPedido);
                throw new RuntimeException(
                        "El monto del pago (" + monto + ") no coincide con el total del pedido (" + totalPedido + ")");
            }

            log.info("Monto validado: {} == {}", monto, totalPedido);

            Optional<Pago> pagoExistente = pagoRepository.findByPaymentId(paymentId);
            if (pagoExistente.isPresent()) {
                log.warn("Pago ya procesado previamente (paymentId={}). Ignorando.", paymentId);
                return;
            }

            pedido.setEstado("PAGADO");
            pedido.setMetodoPago("TARJETA_CREDITO");
            pedidoRepository.save(pedido);

            Pago pago = new Pago();
            pago.setPedido(pedido);
            pago.setMetodoPago("TARJETA_CREDITO");
            pago.setMonto(monto.multiply(BigDecimal.valueOf(100)).intValue());
            pago.setPaymentId(paymentId);
            pago.setEmail(email != null ? email : "sandbox@test.com");
            pago.setEstado("COMPLETADO");
            pago.setFechaPago(LocalDateTime.now());
            pagoRepository.save(pago);

            log.info("Pedido y Pago actualizados en BD correctamente.");

            try {
                log.info("Enviando correo a: {}", pedido.getUsuario().getEmail());
                emailService.enviarCorreoConfirmacion(
                        pedido.getUsuario().getEmail(),
                        pedido.getUsuario().getNombre(),
                        pedido.getIdPedido(),
                        monto.doubleValue()
                );
                log.info("Correo enviado con éxito.");
            } catch (Exception e) {
                log.warn("Correo falló: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error actualizando pedido", e);
            throw new RuntimeException("Error actualizando pedido: " + e.getMessage(), e);
        }
    }
}
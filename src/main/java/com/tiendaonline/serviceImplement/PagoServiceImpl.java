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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import com.mercadopago.resources.merchantorder.MerchantOrderPayment;


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
        System.out.println("=== VERIFICANDO TOKEN ===");
        System.out.println("Token (primeros 15 chars): " + (mpToken != null ? mpToken.substring(0, Math.min(15, mpToken.length())) : "null"));
        System.out.println("Empieza con APP_USR?: " + (mpToken != null && mpToken.startsWith("APP_USR-")));
        System.out.println("Empieza con TEST?: " + (mpToken != null && mpToken.startsWith("TEST-")));
        MercadoPagoConfig.setAccessToken(mpToken);
    }

    @Override
    public PreferenciaResponseDTO crearPreferencia(PreferenciaRequestDTO request) {
        System.out.println("====== ENTRANDO A CREAR PREFERENCIA ======");
        System.out.println("Pedido ID: " + request.getPedidoId());
        System.out.println("Monto: " + request.getMonto());
        System.out.println("Email: " + request.getEmail());

        if (request.getPedidoId() == null) {
            throw new IllegalArgumentException("El ID del pedido no puede ser nulo.");
        }

        try {
            BigDecimal monto = request.getMonto();
            if (monto == null || monto.compareTo(BigDecimal.ZERO) == 0) {
                Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
                monto = pedido.getTotal();
                System.out.println("Monto recuperado del pedido: " + monto);
            }

            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("El monto del pedido es inválido: " + monto);
            }

            // SOLUCIÓN AL BLOQUEO: Generar una referencia única por cada intento de pago.
            // Ejemplo resultado: "176-1718416800" (ID del pedido + timestamp actual)
            String intentoPagoUnicoId = request.getPedidoId() + "-" + System.currentTimeMillis();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(intentoPagoUnicoId) // ← Identificador único para este intento
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
                    .externalReference(intentoPagoUnicoId) // ← Clave para evitar el error de duplicidad
                    .notificationUrl(mpDomain + "/api/pagos/webhook")
                    .binaryMode(false) // ← Recomendado cambiarlo a false en producción para que el motor antifraude pueda mandar a "mediación/review" en vez de rechazar en seco
                    .build();

            Preference preference = new PreferenceClient().create(preferenceRequest);

            System.out.println("=== PREFERENCIA CREADA CORRECTAMENTE ===");
            System.out.println("Init Point (Producción): " + preference.getInitPoint());

            PreferenciaResponseDTO response = new PreferenciaResponseDTO();
            response.setPreferenceId(preference.getId());
            response.setInitPoint(preference.getInitPoint());
            response.setSandboxUrl(null);

            return response;

        } catch (Exception e) {
            System.out.println("====== ERROR AL CREAR PREFERENCIA ======");
            e.printStackTrace();
            throw new RuntimeException("Error al crear preferencia: " + e.getMessage(), e);
        }
    }

    @Override
    public void procesarWebhook(String id) {
        System.out.println("====== PROCESANDO WEBHOOK ======");
        System.out.println("ID recibido: " + id);

        MPRequestOptions requestOptions = MPRequestOptions.builder()
                .accessToken(mpToken)
                .build();

        try {
            // Intentar como Payment primero
            try {
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(Long.parseLong(id), requestOptions);

                System.out.println("💰 Procesando como PAGO. Estado: " + payment.getStatus());

                // Solo procesar si está aprobado
                if ("approved".equals(payment.getStatus())) {
                    actualizarPedido(
                            String.valueOf(payment.getId()),
                            payment.getExternalReference(),
                            payment.getTransactionAmount(),
                            payment.getPayer() != null ? payment.getPayer().getEmail() : null
                    );
                } else {
                    System.out.println("⚠️ Pago no aprobado. Estado: " + payment.getStatus() + " - No se actualiza el pedido");
                    // No hacer nada con pagos rechazados/pendientes
                    return;
                }
                return;

            } catch (MPApiException e) {
                if (e.getStatusCode() != 404) {
                    System.out.println("⚠️ Error API (no 404): " + e.getMessage());
                    // No lanzar excepción, solo loguear
                    return;
                }
                System.out.println("ℹ️ El ID no corresponde a un Pago individual (404). Evaluando como MerchantOrder...");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ ID no es numérico: " + id);
                return;
            }

            // Intentar como MerchantOrder
            try {
                MerchantOrderClient orderClient = new MerchantOrderClient();
                MerchantOrder order = orderClient.get(Long.parseLong(id), requestOptions);
                System.out.println("📦 Procesando como ORDEN. Estado de la orden: " + order.getStatus());

                if (order.getPayments() != null && !order.getPayments().isEmpty()) {
                    for (MerchantOrderPayment mop : order.getPayments()) {
                        System.out.println("  Pago en orden - ID: " + mop.getId() + ", Estado: " + mop.getStatus());

                        if ("approved".equals(mop.getStatus())) {
                            System.out.println("✅ Pago aprobado encontrado dentro de la orden: " + mop.getId());
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
                    System.out.println("⚠️ Ningún pago aprobado en la orden.");
                } else {
                    System.out.println("⚠️ La orden no tiene pagos asociados.");
                }

            } catch (MPApiException e) {
                System.out.println("⚠️ Error obteniendo orden (MPApiException): " + e.getMessage());
                // No lanzar excepción
            } catch (NumberFormatException e) {
                System.out.println("⚠️ ID no es numérico para MerchantOrder: " + id);
            } catch (Exception e) {
                System.out.println("⚠️ Error procesando orden: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("====== ERROR GENERAL EN WEBHOOK ======");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            // NO lanzar RuntimeException para evitar que MP reintente innecesariamente
        }
    }

    @Override
    public void procesarWebhookConDatos(String paymentId, String externalReference,
                                        BigDecimal monto, String email) {
        System.out.println("====== PROCESANDO PAGO DIRECTO DESDE WEBHOOK ======");
        System.out.println("Payment ID: " + paymentId);
        System.out.println("External Reference (Pedido ID): " + externalReference);
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
            System.out.println("Actualizando Pedido ID Real: " + pedidoId);

            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

            // ... El resto de tu lógica de actualización de base de datos se mantiene EXACTAMENTE IGUAL ...
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

            System.out.println("✅ Pedido y Pago actualizados en BD correctamente.");

            try {
                System.out.println("Enviando correo a: " + pedido.getUsuario().getEmail());
                emailService.enviarCorreoConfirmacion(
                        pedido.getUsuario().getEmail(),
                        pedido.getUsuario().getNombre(),
                        pedido.getIdPedido(),
                        monto.doubleValue()
                );
                System.out.println("✅ Correo enviado con éxito.");
            } catch (Exception e) {
                System.err.println("⚠️ Correo falló: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("====== ERROR ACTUALIZANDO PEDIDO ======");
            e.printStackTrace();
            throw new RuntimeException("Error actualizando pedido: " + e.getMessage(), e);
        }
    }

}
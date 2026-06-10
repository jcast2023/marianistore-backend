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
                monto = new BigDecimal(String.valueOf(pedido.getTotal()));
            }

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(String.valueOf(request.getPedidoId()))
                    .title(request.getDescripcion() != null && !request.getDescripcion().trim().isEmpty()
                            ? request.getDescripcion()
                            : "Pedido #" + request.getPedidoId())
                    .description("Artículos del Pedido #" + request.getPedidoId())
                    .quantity(1)
                    .unitPrice(monto)
                    .currencyId("PEN")
                    .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(request.getEmail())
                    .name(request.getNombre())
                    .surname(request.getApellido())
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
                    .statementDescriptor("MarianiStore")
                    .externalReference(String.valueOf(request.getPedidoId()))
                    .notificationUrl(mpDomain + "/api/pagos/webhook")
                    .build();

            Preference preference = new PreferenceClient().create(preferenceRequest);

            System.out.println("=== PREFERENCIA CREADA CORRECTAMENTE ===");
            System.out.println("Sandbox Init Point Forzado: " + preference.getSandboxInitPoint());

            PreferenciaResponseDTO response = new PreferenciaResponseDTO();
            response.setPreferenceId(preference.getId());

            // ⚡ CONTROL DE CERTIFICACIÓN: Forzamos la URL de Sandbox en ambas propiedades
            response.setInitPoint(preference.getSandboxInitPoint());
            response.setSandboxUrl(preference.getSandboxInitPoint());

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
            try {
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(Long.parseLong(id), requestOptions);

                System.out.println("💰 Procesando como PAGO. Estado: " + payment.getStatus());
                if ("approved".equals(payment.getStatus())) {
                    actualizarPedido(
                            String.valueOf(payment.getId()),
                            payment.getExternalReference(),
                            payment.getTransactionAmount(),
                            payment.getPayer() != null ? payment.getPayer().getEmail() : null
                    );
                    return;
                }
            } catch (MPApiException e) {
                if (e.getStatusCode() != 404) {
                    throw e;
                }
                System.out.println("ℹ️ El ID no corresponde a un Pago individual (404). Evaluando como MerchantOrder...");
            }

            MerchantOrderClient orderClient = new MerchantOrderClient();
            MerchantOrder order = orderClient.get(Long.parseLong(id), requestOptions);
            System.out.println("📦 Procesando como ORDEN. Estado de la orden: " + order.getStatus());

            if (order.getPayments() != null && !order.getPayments().isEmpty()) {
                for (MerchantOrderPayment mop : order.getPayments()) {
                    if ("approved".equals(mop.getStatus())) {
                        System.out.println("✅ Pago aprobado encontrado dentro de la orden: " + mop.getId());
                        actualizarPedido(
                                String.valueOf(mop.getId()),
                                order.getExternalReference(),
                                mop.getTransactionAmount(),
                                order.getPayer() != null && order.getPayer().getId() != null ? "usuario_mp_" + order.getPayer().getId() + "@test.com" : null
                        );
                        return;
                    }
                }
            }
            System.out.println("⚠️ La orden aún no cuenta con un pago aprobado.");

        } catch (Exception e) {
            System.out.println("====== ERROR PROCESANDO WEBHOOK ======");
            e.printStackTrace();
            throw new RuntimeException("Error procesando webhook: " + e.getMessage(), e);
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
            Integer pedidoId = Integer.parseInt(externalReference);
            System.out.println("Actualizando Pedido ID: " + pedidoId);

            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

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
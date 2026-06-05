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
        System.out.println("Pedido ID recibido: " + request.getPedidoId());
        System.out.println("Monto recibido: " + request.getMonto());

        if (request.getPedidoId() == null) {
            throw new IllegalArgumentException("El ID del pedido no puede ser nulo.");
        }

        try {
            System.out.println("Construyendo ítem de Mercado Pago...");
            BigDecimal monto = request.getMonto();
            if (monto == null || monto.compareTo(BigDecimal.ZERO) == 0) {
                Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
                monto = new BigDecimal(String.valueOf(pedido.getTotal()));
            }

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(request.getDescripcion())
                    .quantity(1)
                    .unitPrice(monto)
                    .currencyId("PEN")
                    .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(request.getEmail() != null ? request.getEmail() : "comprador@marianistore.com")
                    .build();

            System.out.println("Ítem construido con éxito.");
            System.out.println("Configurando URLs de retorno...");

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://marianistore-frontend.vercel.app/mis-pedidos")
                    .failure("https://marianistore-frontend.vercel.app/mis-pedidos")
                    .pending("https://marianistore-frontend.vercel.app/mis-pedidos")
                    .build();

            System.out.println("Enviando preferencia a la API de Mercado Pago...");

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(Collections.singletonList(item))
                    .payer(payer)
                    .backUrls(backUrls)
                    .externalReference(String.valueOf(request.getPedidoId()))
                    .notificationUrl(mpDomain + "/api/pagos/webhook")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            System.out.println("=== URLS DE MERCADO PAGO GENERADAS ===");
            System.out.println("initPoint:  " + preference.getInitPoint());
            System.out.println("sandboxUrl: " + preference.getSandboxInitPoint());
            System.out.println("======================================");

            PreferenciaResponseDTO response = new PreferenciaResponseDTO();
            response.setPreferenceId(preference.getId());
            response.setInitPoint(preference.getInitPoint());
            response.setSandboxUrl(preference.getSandboxInitPoint());

            return response;

        } catch (MPApiException apiException) {
            System.out.println("====== ERROR DETALLADO DE MERCADO PAGO ======");
            System.out.println("Código HTTP: " + apiException.getStatusCode());
            if (apiException.getApiResponse() != null) {
                System.out.println("Contenido: " + apiException.getApiResponse().getContent());
            }
            System.out.println("=============================================");
            throw new RuntimeException("Error en la API de Mercado Pago: " + apiException.getMessage(), apiException);
        } catch (Exception e) {
            System.out.println("====== ERROR GENÉRICO EN BACKEND ======");
            e.printStackTrace();
            throw new RuntimeException("Error al crear preferencia: " + e.getMessage(), e);
        }
    }

    @Override
    public void procesarWebhook(String paymentId) {
        System.out.println("====== PROCESANDO WEBHOOK ======");
        System.out.println("Payment ID recibido: " + paymentId);
        try {
            MPRequestOptions requestOptions = MPRequestOptions.builder()
                    .accessToken(mpToken)
                    .build();

            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.parseLong(paymentId), requestOptions);

            System.out.println("Estado del pago: " + payment.getStatus());

            if ("approved".equals(payment.getStatus())) {
                actualizarPedido(
                        paymentId,
                        payment.getExternalReference(),
                        payment.getTransactionAmount(),
                        payment.getPayer() != null ? payment.getPayer().getEmail() : null
                );
            } else {
                System.out.println("Estado no aprobado: " + payment.getStatus());
            }

        } catch (MPApiException apiEx) {
            System.out.println("Error MP API - Código: " + apiEx.getStatusCode());
            if (apiEx.getApiResponse() != null) {
                System.out.println("Contenido: " + apiEx.getApiResponse().getContent());
            }
            if (apiEx.getStatusCode() == 404) {
                System.out.println("⚠️ Pago no encontrado (404) - ignorando en sandbox");
                return;
            }
            throw new RuntimeException("Error MP: " + apiEx.getMessage(), apiEx);
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
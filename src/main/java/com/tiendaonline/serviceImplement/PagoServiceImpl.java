package com.tiendaonline.serviceImplement;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    @Value("${mercadopago.access.token}")
    private String mpToken;

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final EmailService emailService;

    @PostConstruct
    public void initMP() {
        MercadoPagoConfig.setAccessToken(mpToken);
    }

    @Override
    public PreferenciaResponseDTO crearPreferencia(PreferenciaRequestDTO request) {
        if (request.getPedidoId() == null) {
            throw new IllegalArgumentException("El ID del pedido no puede ser nulo.");
        }

        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(request.getDescripcion())
                    .quantity(1)
                    .unitPrice(request.getMonto())
                    .currencyId("PEN")
                    .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email("comprador@marianistore.com")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:4200/my-orders")
                    .failure("http://localhost:4200/my-orders")
                    .pending("http://localhost:4200/my-orders")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .payer(payer)
                    .backUrls(backUrls)
                    .externalReference(String.valueOf(request.getPedidoId()))
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            System.out.println("=== URLS DE MERCADO PAGO ===");
            System.out.println("initPoint:  " + preference.getInitPoint());
            System.out.println("sandboxUrl: " + preference.getSandboxInitPoint());
            System.out.println("============================");

            PreferenciaResponseDTO response = new PreferenciaResponseDTO();
            response.setPreferenceId(preference.getId());
            response.setInitPoint(preference.getInitPoint());
            response.setSandboxUrl(preference.getSandboxInitPoint());
            return response;

        } catch (MPApiException apiException) {
            System.out.println("====== ERROR DETALLADO DE MERCADO PAGO ======");
            System.out.println(apiException.getApiResponse().getContent());
            System.out.println("=============================================");
            throw new RuntimeException("Error en la API de Mercado Pago: " + apiException.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al crear preferencia: " + e.getMessage());
        }
    }

    @Override
    public void procesarWebhook(String paymentId) {
        try {
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.parseLong(paymentId));

            if ("approved".equals(payment.getStatus())) {
                Integer pedidoId = Integer.parseInt(payment.getExternalReference());

                Pedido pedido = pedidoRepository.findById(pedidoId)
                        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

                pedido.setEstado("PAGADO");
                pedido.setMetodoPago("TARJETA_CREDITO");
                pedidoRepository.save(pedido);

                Pago pago = new Pago();
                pago.setPedido(pedido);
                pago.setMetodoPago("TARJETA_CREDITO");
                pago.setMonto(payment.getTransactionAmount().multiply(BigDecimal.valueOf(100)).intValue());
                pago.setPaymentId(paymentId);
                pago.setEmail(payment.getPayer().getEmail());
                pago.setEstado("COMPLETADO");
                pago.setFechaPago(LocalDateTime.now());
                pagoRepository.save(pago);

                // ✅ CORREO después de pago aprobado por MP
                try {
                    emailService.enviarCorreoConfirmacion(
                            pedido.getUsuario().getEmail(),
                            pedido.getUsuario().getNombre(),
                            pedido.getIdPedido(),
                            payment.getTransactionAmount().doubleValue()
                    );
                } catch (Exception e) {
                    System.err.println("⚠️ Webhook procesado, pero falló el correo: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error procesando webhook: " + e.getMessage());
        }
    }
}
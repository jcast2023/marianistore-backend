package com.tiendaonline.serviceImplement;

import com.tiendaonline.model.Reclamo;
import com.tiendaonline.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void enviarCorreoConfirmacion(String emailDestino, String nombreCliente, Object idPedido, Double totalPedido) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(emailDestino);
            // Agregamos una estampa de tiempo al asunto para evitar que Gmail agrupe los correos de prueba
            String horaActual = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            helper.setSubject("🛒 ¡Tu pedido #" + idPedido + " ha sido confirmado! (" + horaActual + ")");

            // Estructura HTML real del correo con estilos inline
            String cuerpoHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eeeeee; border-radius: 8px;'>"
                    + "  <div style='text-align: center; background-color: #007bff; padding: 15px; border-radius: 6px 6px 0 0;'>"
                    + "    <h2 style='color: white; margin: 0;'>¡Gracias por tu compra!</h2>"
                    + "  </div>"
                    + "  <div style='padding: 20px; color: #333333; line-height: 1.6;'>"
                    + "    <p>Hola <strong>" + nombreCliente + "</strong>,</p>"
                    + "    <p>Te informamos que hemos recibido tu pedido correctamente y ya nos encontramos trabajando en su preparación.</p>"
                    + "    "
                    + "    <div style='background-color: #f8f9fa; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #007bff;'>"
                    + "      <h4 style='margin-top: 0; color: #007bff;'>Resumen de la Orden</h4>"
                    + "      <p style='margin: 5px 0;'><strong>Número de Pedido:</strong> #" + idPedido + "</p>"
                    + "      <p style='margin: 5px 0;'><strong>Estado:</strong> Confirmado / Procesando</p>"
                    + "      <p style='margin: 5px 0; font-size: 16px;'><strong>Total Pagado:</strong> <span style='color: #28a745; font-weight: bold;'>S/ " + String.format("%.2f", totalPedido) + "</span></p>"
                    + "    </div>"
                    + "    "
                    + "    <p>Pronto nos comunicaremos contigo para coordinar los detalles de la entrega o el despacho de tus productos.</p>"
                    + "    <p style='margin-bottom: 0;'>Si tienes alguna duda o consulta sobre tu orden, responde directamente a este correo.</p>"
                    + "  </div>"
                    + "  <div style='text-align: center; padding-top: 20px; border-top: 1px solid #eeeeee; font-size: 12px; color: #777777;'>"
                    + "    <p>© 2026 TechShop. Todos los derechos reservados.</p>"
                    + "    <p>Chorrillos, Lima - Perú</p>"
                    + "  </div>"
                    + "</div>";

            helper.setText(cuerpoHtml, true);
            mailSender.send(message);
            System.out.println("👉 Correo enviado con éxito a: " + emailDestino);

        } catch (MessagingException e) {
            System.err.println("❌ Error al estructurar el correo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
        }
    }

    // --- 🆕 NUEVO MÉTODO ASÍNCRONO (LIBRO DE RECLAMACIONES) ---
    @Async // Run en segundo plano para no congelar la respuesta del backend
    @Override
    public void enviarCorreoReclamo(Reclamo reclamo) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(reclamo.getEmail());
            helper.setSubject("📋 MarianíStore - Copia de Hoja de Reclamación Virtual " + reclamo.getCodigoReclamo());

            String fechaFormateada = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
                    .format(reclamo.getFechaRegistro());
            // Estructura HTML corporativa con el color verde oscuro (#03291c) que usas en tus botones
            String cuerpoHtml = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eeeeee; border-radius: 8px;">
                  <div style="text-align: center; background-color: #03291c; padding: 15px; border-radius: 6px 6px 0 0; color: white;">
                    <h2 style="margin: 0; font-size: 20px;">Libro de Reclamaciones Virtual</h2>
                    <p style="margin: 5px 0 0 0; font-size: 12px; opacity: 0.8;">Hoja de Reclamación Virtual - Ley N° 29571</p>
                  </div>
                  
                  <div style="padding: 20px; color: #333333; line-height: 1.6; font-size: 14px;">
                    <p>Estimado(a) <strong>%s %s</strong>,</p>
                    <p>Confirmamos la recepción de su reclamo/queja en nuestra plataforma. A continuación, le adjuntamos una copia completa de los hechos registrados:</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #03291c;">
                      <h4 style="margin-top: 0; color: #03291c; margin-bottom: 10px;">Resumen de la Reclamación</h4>
                      <p style="margin: 4px 0;"><strong>Código de Seguimiento:</strong> <span style="color: #c53030; font-weight: bold;">%s</span></p>
                      <p style="margin: 4px 0;"><strong>Fecha de Registro:</strong> %s</p>
                      <p style="margin: 4px 0;"><strong>Tipo:</strong> %s</p>
                      <p style="margin: 4px 0;"><strong>Bien afectado:</strong> %s (Pedido: %s)</p>
                      <p style="margin: 4px 0;"><strong>Monto Reclamado:</strong> S/ %s</p>
                    </div>
                    
                    <p style="margin-bottom: 5px;"><strong>Detalle de los hechos explicados:</strong></p>
                    <div style="background-color: #ffffff; padding: 10px; border: 1px solid #e2e8f0; border-radius: 4px; font-size: 13px; color: #4a5568; margin-bottom: 15px; text-align: justify;">
                      %s
                    </div>
                    
                    <p style="margin-bottom: 5px;"><strong>Pedido concreto solicitado:</strong></p>
                    <div style="background-color: #ffffff; padding: 10px; border: 1px solid #e2e8f0; border-radius: 4px; font-size: 13px; color: #4a5568; margin-bottom: 20px; text-align: justify;">
                      %s
                    </div>
                    
                    <div style="background-color: #fffaf0; border-left: 4px solid #dd6b20; padding: 12px; font-size: 12px; color: #7b341e; border-radius: 4px;">
                      <strong>Plazo de ley:</strong> De conformidad con las normas de INDECOPI, daremos respuesta definitiva a su requerimiento en un plazo no mayor a quince (15) días hábiles.
                    </div>
                  </div>
                  
                  <div style="text-align: center; padding-top: 20px; border-top: 1px solid #eeeeee; font-size: 11px; color: #777777;">
                    <p><strong>MarianíStore</strong> | Operado por Meyten S.A.C. RUC: 20602863116</p>
                    <p>Jr. Ucayali 768, Lima - Perú</p>
                  </div>
                </div>
                """.formatted(
                    reclamo.getNombres(),
                    reclamo.getApellidos(),
                    reclamo.getCodigoReclamo(),
                    fechaFormateada,
                    reclamo.getQuejaReclamo(),
                    reclamo.getTipoBien(),
                    reclamo.getPedido(),
                    String.format("%.2f", reclamo.getMontoReclamado()),
                    reclamo.getDetalle(),
                    reclamo.getPedidoConsumidor()
            );

            helper.setText(cuerpoHtml, true);
            mailSender.send(message);
            System.out.println("👉 Correo de reclamación enviado con éxito a: " + reclamo.getEmail());

        } catch (MessagingException e) {
            System.err.println("❌ Error al estructurar el correo del reclamo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado en email de reclamo: " + e.getMessage());
        }
    }
}

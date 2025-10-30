package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envía un correo electrónico con un archivo PDF adjunto (por ejemplo, un recibo de venta).
     *
     * @param destinatario  Dirección de correo del cliente.
     * @param asunto        Asunto del correo.
     * @param contenidoHtml Contenido del correo en formato HTML.
     * @param rutaAdjunto   Ruta del archivo PDF a adjuntar.
     */
    public void enviarCorreoConAdjunto(String destinatario, String asunto, String contenidoHtml, String rutaAdjunto) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            // ✅ Datos básicos del correo
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true); // true = permite HTML

            // ✅ Si existe el archivo adjunto, lo agrega
            if (rutaAdjunto != null && !rutaAdjunto.isBlank()) {
                File archivo = new File(rutaAdjunto);
                if (archivo.exists()) {
                    FileSystemResource recurso = new FileSystemResource(archivo);
                    helper.addAttachment(recurso.getFilename(), recurso);
                } else {
                    System.err.println("⚠️ Archivo adjunto no encontrado: " + rutaAdjunto);
                }
            }

            // ✅ Envío del correo
            mailSender.send(mensaje);
            System.out.println("✅ Correo enviado exitosamente a " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al enviar correo: " + e.getMessage());
        }
    }
}

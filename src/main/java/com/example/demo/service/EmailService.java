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
     * Envía un correo con un archivo PDF adjunto (por ejemplo, el recibo de venta).
     */
    public void enviarCorreoConAdjunto(String destinatario, String asunto, String contenidoHtml, String rutaAdjunto) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true); // Permite contenido HTML

            if (rutaAdjunto != null) {
                File archivo = new File(rutaAdjunto);
                if (archivo.exists()) {
                    FileSystemResource resource = new FileSystemResource(archivo);
                    helper.addAttachment(resource.getFilename(), resource);
                } else {
                    System.err.println("⚠️ Archivo no encontrado: " + rutaAdjunto);
                }
            }

            mailSender.send(mensaje);
            System.out.println("✅ Correo enviado exitosamente a " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

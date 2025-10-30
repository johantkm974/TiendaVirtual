package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoConAdjunto(String destinatario, String asunto, String contenidoHtml, byte[] pdfBytes, String nombreArchivo) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            if (pdfBytes != null) {
                helper.addAttachment(nombreArchivo, new ByteArrayResource(pdfBytes));
            }

            mailSender.send(mensaje);
            System.out.println("✅ Correo enviado exitosamente a " + destinatario);

        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


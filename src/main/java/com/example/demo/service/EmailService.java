package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarReciboPorCorreo(String destinatario, String pdfUrl) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recibo de tu compra - Tienda Virtual");
        mensaje.setText("Gracias por tu compra.\n\nPuedes descargar tu recibo en el siguiente enlace:\n" + pdfUrl);
        mailSender.send(mensaje);
    }

    // ✅ Método faltante para compatibilidad con controladores antiguos
    public void enviarCorreoConAdjunto(String destinatario, String asunto, String cuerpo, String adjuntoUrl) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo + "\n\nDescarga el archivo adjunto aquí: " + adjuntoUrl);
        mailSender.send(mensaje);
    }
}

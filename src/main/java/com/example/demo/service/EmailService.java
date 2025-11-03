package com.example.demo.service;

import com.example.demo.config.BrevoApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final RestTemplate restTemplate;
    private final BrevoApiConfig.BrevoApiCredentials brevoCredentials;

    @Autowired
    public EmailService(RestTemplate restTemplate, BrevoApiConfig.BrevoApiCredentials brevoCredentials) {
        this.restTemplate = restTemplate;
        this.brevoCredentials = brevoCredentials;
    }

    public void enviarReciboPorCorreo(String destinatario, String pdfUrl) {
        String asunto = "Recibo de tu compra - Tienda Virtual";
        String contenido = generarContenidoRecibo(destinatario, pdfUrl);
        enviarCorreoBrevo(destinatario, asunto, contenido);
    }

    public void enviarCorreoConAdjunto(String destinatario, String asunto, String cuerpo, String adjuntoUrl) {
        String contenido = generarContenidoConAdjunto(cuerpo, adjuntoUrl);
        enviarCorreoBrevo(destinatario, asunto, contenido);
    }

    private void enviarCorreoBrevo(String destinatario, String asunto, String contenidoHtml) {
        // Validar configuración primero
        if (!brevoCredentials.isConfigured()) {
            System.out.println("❌ BREVO_API_KEY no configurada. Correo no enviado.");
            System.out.println("ℹ️ Verifica que BREVO_API_KEY esté configurada en Railway");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoCredentials.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            
            // Remitente desde configuración
            Map<String, String> sender = new HashMap<>();
            sender.put("name", brevoCredentials.getFromName());
            sender.put("email", brevoCredentials.getFromEmail());
            requestBody.put("sender", sender);
            
            // Destinatario
            Map<String, String> to = new HashMap<>();
            to.put("email", destinatario);
            requestBody.put("to", new Map[]{to});
            
            // Asunto y contenido
            requestBody.put("subject", asunto);
            requestBody.put("htmlContent", contenidoHtml);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            System.out.println("📧 Intentando enviar correo a: " + destinatario);

            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.brevo.com/v3/smtp/email", 
                HttpMethod.POST, 
                request, 
                String.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("✅ Correo enviado exitosamente a: " + destinatario);
            } else {
                System.out.println("⚠️ Respuesta inesperada de Brevo: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error al enviar correo vía API Brevo: " + e.getMessage());
            // Manejo de errores específicos de Brevo
            if (e.getMessage().contains("401")) {
                System.out.println("🔐 Error de autenticación - BREVO_API_KEY inválida");
            } else if (e.getMessage().contains("402")) {
                System.out.println("💳 Límite de crédito excedido en Brevo");
            } else if (e.getMessage().contains("400")) {
                System.out.println("📧 Error en la solicitud - verifica el formato del email");
            }
        }
    }

    // Mantener los mismos métodos de generación de contenido...
    private String generarContenidoRecibo(String destinatario, String pdfUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }
                    .button { display: inline-block; padding: 12px 24px; background: #4CAF50; color: white; text-decoration: none; border-radius: 4px; margin: 10px 0; }
                    .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #777; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>¡Gracias por tu compra!</h1>
                    </div>
                    <div class="content">
                        <p>Hola,</p>
                        <p>Tu compra ha sido procesada exitosamente. Aquí tienes los detalles:</p>
                        
                        <p><strong>Puedes descargar tu recibo en el siguiente enlace:</strong></p>
                        <p><a href="%s" class="button">Descargar Recibo PDF</a></p>
                        
                        <p>Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
                        <p>%s</p>
                        
                        <div class="footer">
                            <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                            <p>&copy; 2024 Tienda Virtual. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(pdfUrl, pdfUrl);
    }
    private String generarContenidoConAdjunto(String cuerpo, String adjuntoUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .button { display: inline-block; padding: 12px 24px; background: #4CAF50; color: white; text-decoration: none; border-radius: 4px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <p>%s</p>
                    <p><strong>Descarga el archivo adjunto:</strong></p>
                    <p><a href="%s" class="button">Descargar Archivo</a></p>
                    <p>Enlace directo: %s</p>
                </div>
            </body>
            </html>
            """.formatted(cuerpo.replace("\n", "<br>"), adjuntoUrl, adjuntoUrl);
    }
}




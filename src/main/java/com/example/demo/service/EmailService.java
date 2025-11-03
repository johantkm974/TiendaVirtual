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

    // ✅ Nuevo método para enviar correo con PDF adjunto
    public void enviarCorreoConAdjunto(String destinatario, String asunto, String cuerpo, String adjuntoUrl) {
        enviarCorreoBrevo(destinatario, asunto, generarCorreoConAdjuntoHTML(cuerpo, adjuntoUrl));
    }

    // ✅ Método existente (recibo simple)
    public void enviarReciboPorCorreo(String destinatario, String pdfUrl) {
        String asunto = "Recibo de tu compra - Tienda Virtual";
        String contenido = generarContenidoRecibo(pdfUrl);
        enviarCorreoBrevo(destinatario, asunto, contenido);
    }

    // 🔧 Envío genérico a Brevo
    private void enviarCorreoBrevo(String destinatario, String asunto, String htmlContent) {
        if (!brevoCredentials.isConfigured()) {
            System.out.println("❌ BREVO_API_KEY no configurada.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoCredentials.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, String> sender = new HashMap<>();
            sender.put("name", brevoCredentials.getFromName());
            sender.put("email", brevoCredentials.getFromEmail());
            requestBody.put("sender", sender);

            Map<String, String> to = new HashMap<>();
            to.put("email", destinatario);
            requestBody.put("to", new Map[]{to});

            requestBody.put("subject", asunto);
            requestBody.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.brevo.com/v3/smtp/email", HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED)
                System.out.println("✅ Correo enviado exitosamente a: " + destinatario);
            else
                System.out.println("⚠️ Respuesta inesperada de Brevo: " + response.getStatusCode());

        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
        }
    }

    // 🧾 HTML del recibo
    private String generarContenidoRecibo(String pdfUrl) {
        return """
        <html><body>
        <h2>Gracias por tu compra</h2>
        <p>Puedes descargar tu recibo aquí:</p>
        <a href='%s'>Descargar PDF</a>
        </body></html>
        """.formatted(pdfUrl);
    }

    // 📎 HTML del correo con adjunto
    private String generarCorreoConAdjuntoHTML(String cuerpo, String adjuntoUrl) {
        return """
        <html><body>
        <p>%s</p>
        <p><a href='%s'>Descargar archivo adjunto (PDF)</a></p>
        </body></html>
        """.formatted(cuerpo, adjuntoUrl);
    }
}


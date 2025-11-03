package com.example.demo.service;

import com.example.demo.config.BrevoApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Envía el PDF generado directamente como adjunto al correo.
     */
    public void enviarReciboAdjunto(String destinatario, String pdfPath, String nombreArchivo) {
        if (!brevoCredentials.isConfigured()) {
            System.out.println("❌ BREVO_API_KEY no configurada. Correo no enviado.");
            return;
        }

        try {
            // Convertir el archivo PDF a Base64
            File file = new File(pdfPath);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String base64File = Base64.getEncoder().encodeToString(fileContent);

            // Configurar encabezados
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoCredentials.getApiKey());

            // Configurar cuerpo del correo
            Map<String, Object> requestBody = new HashMap<>();

            // Remitente
            Map<String, String> sender = new HashMap<>();
            sender.put("name", brevoCredentials.getFromName());
            sender.put("email", brevoCredentials.getFromEmail());
            requestBody.put("sender", sender);

            // Destinatario
            Map<String, String> to = new HashMap<>();
            to.put("email", destinatario);
            requestBody.put("to", List.of(to));

            // Asunto y contenido
            requestBody.put("subject", "Recibo de tu compra - Tienda Virtual");
            requestBody.put("htmlContent", """
                <html>
                <body>
                    <h2 style='color:#4CAF50;'>¡Gracias por tu compra!</h2>
                    <p>Adjunto encontrarás el recibo en formato PDF con los detalles de tu pedido.</p>
                    <p>Guarda este comprobante para tus registros.</p>
                    <hr>
                    <p style='font-size:12px;color:#888;'>© 2025 Tienda Virtual - Envío automático.</p>
                </body>
                </html>
            """);

            // 📎 Adjuntar el archivo PDF
            Map<String, String> attachment = new HashMap<>();
            attachment.put("content", base64File);
            attachment.put("name", nombreArchivo);
            requestBody.put("attachment", List.of(attachment));

            // Enviar solicitud a Brevo
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.brevo.com/v3/smtp/email",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("✅ Correo con recibo adjunto enviado correctamente a: " + destinatario);
            } else {
                System.out.println("⚠️ Respuesta inesperada de Brevo: " + response.getStatusCode());
                System.out.println(response.getBody());
            }

        } catch (Exception e) {
            System.out.println("❌ Error al enviar correo con adjunto: " + e.getMessage());
        }
    }
}



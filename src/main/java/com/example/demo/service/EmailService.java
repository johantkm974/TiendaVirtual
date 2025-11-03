package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final RestTemplate restTemplate;

    // Si usas Brevo/Sender (antes Sendinblue), coloca aquí tu API key y remitente
    // Puedes inyectarlas vía @Value o a través de una clase @ConfigurationProperties
    private final String brevoApiKey;
    private final String fromName;
    private final String fromEmail;

    @Autowired
    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // ⚠️ Ajusta estos valores o inyecta desde application.properties
        this.brevoApiKey = System.getenv("BREVO_API_KEY");   // o @Value("${brevo.apiKey}")
        this.fromName     = "Tienda Virtual";
        this.fromEmail    = "no-reply@tu-dominio.com";
    }

    /**
     * ✅ Método usado por tus controladores anteriores (4 parámetros).
     * Firma exacta: (String, String, String, String)
     * to, subject, htmlBody, adjuntoUrl
     *
     * NOTA: En este ejemplo el “adjunto” se envía como enlace (URL) dentro del HTML.
     * Si luego quieres adjuntar el binario, puedo darte el payload de Brevo con attachments (Base64).
     */
    public void enviarCorreoConAdjunto(String to, String subject, String htmlBody, String adjuntoUrl) {
        String html = """
            <html><body>
              %s
              <p style="margin-top:12px">
                <a href="%s">Descargar adjunto (PDF)</a>
              </p>
            </body></html>
        """.formatted(htmlBody == null ? "" : htmlBody, adjuntoUrl == null ? "#" : adjuntoUrl);

        enviarCorreoBrevo(to, subject, html);
    }

    /**
     * ✅ Método que te falta en VentaService y PagoSimuladoController (3 parámetros).
     * Firma exacta: (String, String, String)
     * to, subject, pdfUrl
     *
     * Equivale a enviar un “recibo” con enlace al PDF.
     */
    public void enviarReciboAdjunto(String to, String subject, String pdfUrl) {
        String html = """
            <html><body>
              <h2>Gracias por tu compra</h2>
              <p>Tu comprobante está listo. Puedes descargarlo aquí:</p>
              <p><a href="%s">Descargar recibo (PDF)</a></p>
            </body></html>
        """.formatted(pdfUrl == null ? "#" : pdfUrl);

        enviarCorreoBrevo(to, subject, html);
    }

    // ============= Infra de envío (Brevo) =============

    private void enviarCorreoBrevo(String to, String subject, String htmlContent) {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            System.err.println("❌ BREVO_API_KEY no configurada. No se envió el correo.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("name", fromName);
            sender.put("email", fromEmail);
            body.put("sender", sender);

            Map<String, String> toEntry = new HashMap<>();
            toEntry.put("email", to);
            body.put("to", new Map[]{toEntry});

            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                "https://api.brevo.com/v3/smtp/email",
                HttpMethod.POST,
                request,
                String.class
            );

            if (resp.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("✅ Correo enviado a " + to);
            } else {
                System.out.println("⚠️ Respuesta Brevo: " + resp.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Error enviando correo: " + e.getMessage());
        }
    }
}


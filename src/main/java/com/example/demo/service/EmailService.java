package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;

import java.util.Base64;
import java.util.Map;

@Service
public class EmailService {

    private final String API_URL = "https://sandbox.api.mailtrap.io/api/send/4141264";
    private final String API_TOKEN = "fdc7b7f494d9b5704864afdb47ebf71c"; // tu token Mailtrap

    public void enviarCorreoConAdjunto(String destinatario, String asunto, String contenidoHtml, byte[] pdfBytes, String nombreArchivo) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(API_TOKEN);

            String pdfBase64 = pdfBytes != null ? Base64.getEncoder().encodeToString(pdfBytes) : null;

            Map<String, Object> body = Map.of(
                    "from", Map.of("email", "hello@example.com", "name", "Mailtrap Test"),
                    "to", new Map[]{ Map.of("email", destinatario) },
                    "subject", asunto,
                    "text", contenidoHtml,
                    "html", contenidoHtml,
                    "attachments", pdfBase64 != null ? new Map[]{ Map.of(
                            "content", pdfBase64,
                            "filename", nombreArchivo,
                            "type", "application/pdf"
                    )} : null
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject(API_URL, request, String.class);

            System.out.println("✅ Correo enviado exitosamente a " + destinatario);

        } catch (Exception e) {
            System.err.println("❌ Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



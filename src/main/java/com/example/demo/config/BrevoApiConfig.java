package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@Configuration
public class BrevoApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Método para obtener variables de entorno con valores por defecto
    public String getEnvVariable(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            System.out.println("⚠️ Variable de entorno '" + key + "' no encontrada, usando valor por defecto");
            return defaultValue;
        }
        // Limpiar comillas si las tiene
        value = value.replace("\"", "").trim();
        System.out.println("✅ Variable '" + key + "' cargada: " + (key.contains("KEY") || key.contains("PASSWORD") ? "***" : value));
        return value;
    }

    @Bean
    public BrevoApiCredentials brevoApiCredentials() {
        String apiKey = getEnvVariable("BREVO_API_KEY", "");
        String fromEmail = getEnvVariable("BREVO_FROM_EMAIL", "no-reply@tutienda.com");
        String fromName = getEnvVariable("BREVO_FROM_NAME", "Tienda Virtual");
        
        return new BrevoApiCredentials(apiKey, fromEmail, fromName);
    }

    // Clase para mantener las credenciales
    public static class BrevoApiCredentials {
        private final String apiKey;
        private final String fromEmail;
        private final String fromName;

        public BrevoApiCredentials(String apiKey, String fromEmail, String fromName) {
            this.apiKey = apiKey;
            this.fromEmail = fromEmail;
            this.fromName = fromName;
        }

        public String getApiKey() { return apiKey; }
        public String getFromEmail() { return fromEmail; }
        public String getFromName() { return fromName; }
        
        public boolean isConfigured() {
            return apiKey != null && !apiKey.trim().isEmpty();
        }
    }
}
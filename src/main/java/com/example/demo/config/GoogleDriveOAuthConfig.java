package com.example.demo.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleDriveOAuthConfig {

    @Bean
    public Drive googleDriveService() throws Exception {
        System.out.println("========= DEBUG GOOGLE DRIVE CONFIG =========");
        System.out.println("GOOGLE_CLIENT_EMAIL: " + System.getenv("GOOGLE_CLIENT_EMAIL"));
        System.out.println("GOOGLE_CLIENT_ID: " + System.getenv("GOOGLE_CLIENT_ID"));
        System.out.println("=============================================");

        String privateKey = System.getenv("GOOGLE_PRIVATE_KEY");

        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("❌ GOOGLE_PRIVATE_KEY no está definida o vacía en las variables de entorno.");
        }

        // 🔍 Limpieza total: eliminar espacios invisibles o caracteres no válidos
        privateKey = privateKey
                .replace("\\r", "")
                .replace("\\n", "\n")  // convierte literales escapados en saltos reales
                .replace("\r", "")      // elimina CR (carriage return) de Windows
                .trim();                // elimina espacios sobrantes

        // Verificar estructura
        if (!privateKey.contains("BEGIN PRIVATE KEY") || !privateKey.contains("END PRIVATE KEY")) {
            System.out.println("⚠️ Advertencia: la clave no tiene el formato PEM esperado.");
        }

        System.out.println("Longitud final de la clave: " + privateKey.length());

        try {
            // ✅ Crear credenciales desde variables limpias
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(
                    System.getenv("GOOGLE_CLIENT_ID"),
                    System.getenv("GOOGLE_CLIENT_EMAIL"),
                    privateKey,
                    null,
                    Collections.singleton(DriveScopes.DRIVE_FILE)
            );

            Drive driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName("Tienda Virtual").build();

            System.out.println("✅ Conexión a Google Drive inicializada correctamente.");
            System.out.println("=============================================");

            return driveService;
        } catch (Exception e) {
            System.out.println("❌ ERROR al inicializar Google Drive:");
            e.printStackTrace();
            throw e;
        }
    }
}


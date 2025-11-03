package com.example.demo.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Configuration
public class GoogleDriveOAuthConfig {

    @Bean
    public Drive googleDriveService() throws Exception {
        // ======== DEBUG opcional ========
        System.out.println("===== GOOGLE DRIVE CONFIG DEBUG =====");
        System.out.println("GOOGLE_CLIENT_EMAIL: " + System.getenv("GOOGLE_CLIENT_EMAIL"));
        System.out.println("GOOGLE_CLIENT_ID: " + System.getenv("GOOGLE_CLIENT_ID"));
        System.out.println("GOOGLE_PRIVATE_KEY (inicio): " +
                (System.getenv("GOOGLE_PRIVATE_KEY") != null
                        ? System.getenv("GOOGLE_PRIVATE_KEY").substring(0, 50) + "..."
                        : "null"));
        System.out.println("=====================================");

        // ✅ Corrige los saltos de línea si Railway los escapó con "\n"
        String privateKey = System.getenv("GOOGLE_PRIVATE_KEY");
        if (privateKey != null) {
            privateKey = privateKey.replace("\\n", "\n");
        } else {
            throw new IllegalStateException("❌ GOOGLE_PRIVATE_KEY no encontrada en variables de entorno");
        }

        // Cargar las credenciales de la cuenta de servicio
        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(
                System.getenv("GOOGLE_CLIENT_ID"),
                System.getenv("GOOGLE_CLIENT_EMAIL"),
                privateKey,
                null,
                Collections.singleton(DriveScopes.DRIVE_FILE)
        );

        // Crear el servicio de Google Drive
        Drive driveService = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("Tienda Virtual").build();

        System.out.println("✅ Conexión a Google Drive inicializada correctamente.");

        return driveService;
    }
}



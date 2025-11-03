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
            throw new IllegalStateException("❌ ERROR: GOOGLE_PRIVATE_KEY no está definida en las variables de entorno.");
        }

        // 🔎 Detección automática de formato
        boolean tieneSaltosEscapados = privateKey.contains("\\n");
        boolean tieneSaltosReales = privateKey.contains("\n");

        System.out.println("Formato detectado:");
        System.out.println("  • Contiene \\n escapados: " + tieneSaltosEscapados);
        System.out.println("  • Contiene saltos reales: " + tieneSaltosReales);

        // ✅ Corrección automática: convierte "\n" en saltos de línea reales si es necesario
        if (tieneSaltosEscapados && !tieneSaltosReales) {
            privateKey = privateKey.replace("\\n", "\n");
            System.out.println("✅ Se reemplazaron los \\n por saltos de línea reales.");
        }

        // ✅ Asegura que la clave empieza y termina correctamente
        if (!privateKey.contains("BEGIN PRIVATE KEY") || !privateKey.contains("END PRIVATE KEY")) {
            throw new IllegalStateException("❌ ERROR: El contenido de GOOGLE_PRIVATE_KEY no tiene formato PEM válido.");
        }

        // 🧠 Crea credenciales de Google Drive
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
    }
}


package com.example.demo.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleDriveConfig {

    // 1️⃣ Inyecta el JSON de credenciales desde variables de entorno
    @Value("${GOOGLE_CREDENTIALS}")
    private String googleCredentialsJson;

    private static final String APPLICATION_NAME = "TiendaVirtual-DriveUploader";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // 2️⃣ Permisos: permite subir y leer archivos
    private static final String SCOPE = DriveScopes.DRIVE_FILE;

    @Bean
    public Drive googleDriveService() throws IOException, GeneralSecurityException {
        // 3️⃣ Cargar credenciales desde el JSON (variable de entorno)
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(googleCredentialsJson.getBytes())
        ).createScoped(Collections.singleton(SCOPE));

        // 4️⃣ Preparar el transporte HTTP
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 5️⃣ Crear y devolver el cliente de Drive
        return new Drive.Builder(
                httpTransport,
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}

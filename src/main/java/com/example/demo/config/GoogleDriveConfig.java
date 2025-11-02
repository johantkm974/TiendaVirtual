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

    // 1. Inyecta el JSON de credenciales desde las variables de entorno
    @Value("${GOOGLE_CREDENTIALS}")
    private String googleCredentialsJson;

    // 2. Define el "Application Name" (puede ser cualquiera)
    private static final String APPLICATION_NAME = "Demo Spring Boot App";
    
    // 3. Define la fábrica de JSON
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // 4. Define los permisos que necesitamos (solo subir archivos)
    private static final String SCOPE = DriveScopes.DRIVE_FILE;

    @Bean
    public Drive googleDriveService() throws IOException, GeneralSecurityException {
        
        // 5. Carga las credenciales desde el String JSON
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(googleCredentialsJson.getBytes())
        ).createScoped(Collections.singleton(SCOPE));

        // 6. Prepara el transporte HTTP
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 7. Construye y retorna el servicio de Drive
        return new Drive.Builder(
                httpTransport,
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
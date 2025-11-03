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
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleDriveOAuthConfig {

    @Bean
    public Drive googleDriveService() throws Exception {
        String privateKey = System.getenv("GOOGLE_PRIVATE_KEY");

        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(
                System.getenv("GOOGLE_CLIENT_ID"),
                System.getenv("GOOGLE_CLIENT_EMAIL"),
                privateKey,
                null,
                Collections.singleton(DriveScopes.DRIVE_FILE)
        );

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("Tienda Virtual").build();
    }
}



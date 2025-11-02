package com.example.demo.service;

import com.example.demo.model.DetalleVenta;
import com.example.demo.model.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PdfGeneratorService {

    // 🔹 Genera el PDF y lo sube a Google Drive
    public String generarReciboPDF(Venta venta) throws Exception {
        String carpeta = "/tmp";
        String nombreArchivo = carpeta + "/recibo_venta_" + venta.getId() + ".pdf";

        // ====== Generar el PDF ======
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(nombreArchivo));
        document.open();

        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph titulo = new Paragraph("Recibo de Venta", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        document.add(new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
        document.add(new Paragraph("Cliente: " + venta.getCliente().getNombre()));
        document.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4, 2, 2, 2});
        String[] headers = {"Producto", "Cantidad", "Precio Unitario", "Subtotal"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabla.addCell(cell);
        }

        for (DetalleVenta detalle : venta.getDetalles()) {
            tabla.addCell(detalle.getProducto().getNombre());
            tabla.addCell(String.valueOf(detalle.getCantidad()));
            tabla.addCell(String.format("S/ %.2f", detalle.getPrecio()));
            tabla.addCell(String.format("S/ %.2f", detalle.getCantidad() * detalle.getPrecio()));
        }

        document.add(tabla);
        document.add(new Paragraph("Total: S/ " + String.format("%.2f", venta.getTotal())));
        document.close();

        // ====== Subir a Google Drive ======
        File pdfFile = new File(nombreArchivo);
        String fileUrl = uploadToGoogleDrive(pdfFile);
        return fileUrl;
    }

    // 🔹 Método para subir el PDF a Google Drive
    private String uploadToGoogleDrive(File filePath) throws Exception {
        // Cargar credenciales desde variable de entorno
        String credentialsJson = System.getenv("GOOGLE_CREDENTIALS");
        if (credentialsJson == null) {
            throw new IllegalStateException("❌ Variable GOOGLE_CREDENTIALS no encontrada.");
        }

        InputStream credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes());
        GoogleCredential credential = GoogleCredential.fromStream(credentialsStream)
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        Drive driveService = new Drive.Builder(
                credential.getTransport(),
                credential.getJsonFactory(),
                credential
        ).setApplicationName("TiendaVirtual").build();

        // Crear metadatos del archivo
        File fileMetadata = new File();
        fileMetadata.setName(filePath.getName());

        // ID de carpeta (desde variable en Railway)
        String folderId = System.getenv("GOOGLE_DRIVE_FOLDER_ID");
        if (folderId != null && !folderId.isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(folderId));
        }

        FileContent mediaContent = new FileContent("application/pdf", filePath);

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink, webContentLink")
                .execute();

        // Retornar enlace de descarga directa
        return "https://drive.google.com/uc?export=download&id=" + uploadedFile.getId();
    }
}



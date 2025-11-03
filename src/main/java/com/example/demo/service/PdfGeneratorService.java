package com.example.demo.service;

import com.example.demo.model.DetalleVenta;
import com.example.demo.model.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@Service
public class PdfGeneratorService {

    @Autowired
    private Drive driveService;

    @Value("${GOOGLE_DRIVE_FOLDER_ID}")
    private String googleDriveFolderId;

    public String generarReciboPDF(Venta venta) throws Exception {
        String carpeta = "/tmp";
        String nombreArchivoLocal = "recibo_venta_" + venta.getId() + ".pdf";
        String rutaCompletaLocal = carpeta + "/" + nombreArchivoLocal;

        // ============================
        // 1️⃣ Generar PDF localmente
        // ============================
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(rutaCompletaLocal));
        document.open();

        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph titulo = new Paragraph("Recibo de Venta", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
        document.add(new Paragraph("Cliente: " + venta.getCliente().getNombre()));
        document.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4, 2, 2, 2});
        String[] encabezados = {"Producto", "Cantidad", "Precio Unitario", "Subtotal"};

        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado));
            celda.setBackgroundColor(BaseColor.LIGHT_GRAY);
            tabla.addCell(celda);
        }

        for (DetalleVenta detalle : venta.getDetalles()) {
            tabla.addCell(detalle.getProducto().getNombre());
            tabla.addCell(String.valueOf(detalle.getCantidad()));
            tabla.addCell(String.format("S/ %.2f", detalle.getPrecio()));
            tabla.addCell(String.format("S/ %.2f", detalle.getCantidad() * detalle.getPrecio()));
        }

        document.add(tabla);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total: S/ " + String.format("%.2f", venta.getTotal())));
        document.close();

        // ============================
        // 2️⃣ Subir a Google Drive
        // ============================
        try {
            if (googleDriveFolderId == null || googleDriveFolderId.isBlank()) {
                throw new IllegalStateException("⚠️ GOOGLE_DRIVE_FOLDER_ID no está definida o está vacía.");
            }

            System.out.println("📂 Subiendo PDF a carpeta ID: " + googleDriveFolderId);

            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(nombreArchivoLocal);
            fileMetadata.setParents(Collections.singletonList(googleDriveFolderId.trim()));

            java.io.File localFile = new java.io.File(rutaCompletaLocal);
            FileContent mediaContent = new FileContent("application/pdf", localFile);

            System.out.println("📦 Metadatos del archivo:");
            System.out.println("  Nombre: " + fileMetadata.getName());
            System.out.println("  Carpeta destino: " + fileMetadata.getParents());

            // ✅ Clave: activar soporte para Shared Drives
            com.google.api.services.drive.model.File uploadedFile = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink")
                    .setSupportsAllDrives(true)
                    .execute();

            // 🔓 Permiso público de lectura
            Permission filePermission = new Permission()
                    .setType("anyone")
                    .setRole("reader");

            driveService.permissions()
                    .create(uploadedFile.getId(), filePermission)
                    .setSupportsAllDrives(true)
                    .execute();

            // 🧹 Eliminar archivo temporal
            if (localFile.exists()) {
                boolean deleted = localFile.delete();
                if (!deleted) System.out.println("⚠️ No se pudo eliminar el archivo temporal: " + rutaCompletaLocal);
            }

            System.out.println("✅ PDF subido correctamente a Drive: " + uploadedFile.getWebViewLink());
            return uploadedFile.getWebViewLink();

        } catch (GoogleJsonResponseException e) {
            System.err.println("❌ Error de Drive (API): " + e.getDetails().getMessage());
            throw new RuntimeException("Error al subir a Drive: " + e.getDetails().getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            throw new RuntimeException("Error al subir a Drive", e);
        }
    }
}


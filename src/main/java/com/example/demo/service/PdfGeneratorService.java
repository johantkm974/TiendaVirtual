package com.example.demo.service;

import com.example.demo.model.DetalleVenta;
import com.example.demo.model.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// ✅ Imports de Google Drive
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
import java.util.List;

@Service
public class PdfGeneratorService {

    @Autowired
    private Drive driveService;

    // ✅ ID de la carpeta en Drive (asegúrate de tenerlo en application.properties o .env)
    @Value("${GOOGLE_DRIVE_FOLDER_ID:1fip_mSbv8XTBkDKTDpSkg3zw52-eYU1x}")
    private String googleDriveFolderId;

    public String generarReciboPDF(Venta venta) throws Exception {
        // 📁 Carpeta temporal (Railway solo permite /tmp)
        String carpeta = "/tmp";
        String nombreArchivoLocal = "recibo_venta_" + venta.getId() + ".pdf";
        String rutaCompletaLocal = carpeta + "/" + nombreArchivoLocal;

        // 1️⃣ Generar el PDF localmente
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(rutaCompletaLocal));
        document.open();

        // 🧾 Encabezado
        Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph titulo = new Paragraph("Recibo de Venta", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Fecha: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
        document.add(new Paragraph("Cliente: " + venta.getCliente().getNombre()));
        document.add(new Paragraph(" "));

        // 🧮 Tabla de productos
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

        // 2️⃣ Subir el archivo a Google Drive
        try {
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(nombreArchivoLocal);
            fileMetadata.setParents(Collections.singletonList(googleDriveFolderId));

            java.io.File localFile = new java.io.File(rutaCompletaLocal);
            FileContent mediaContent = new FileContent("application/pdf", localFile);

            // 🚀 Subida a Drive
            com.google.api.services.drive.model.File uploadedFile = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink")
                    .execute();

            // 🌍 Hacer público el archivo
            Permission filePermission = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            driveService.permissions().create(uploadedFile.getId(), filePermission).execute();

            // 🧹 Borrar archivo temporal
            localFile.delete();

            System.out.println("✅ Archivo subido correctamente a Drive: " + uploadedFile.getWebViewLink());
            return uploadedFile.getWebViewLink();

        } catch (GoogleJsonResponseException e) {
            System.err.println("❌ Error de Drive: " + e.getDetails().getMessage());
            throw new RuntimeException("Error al subir a Drive: " + e.getDetails().getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error general al subir PDF a Drive: " + e.getMessage());
            throw e;
        }
    }

    // ✅ (Opcional) Verificador rápido de carpeta
    public void verificarCarpetaDrive() {
        try {
            System.out.println("🔎 Verificando acceso a carpeta: " + googleDriveFolderId);
            Drive.Files.List request = driveService.files().list()
                    .setQ("'" + googleDriveFolderId + "' in parents and trashed = false")
                    .setFields("files(id, name)")
                    .setPageSize(3);

            List<com.google.api.services.drive.model.File> archivos = request.execute().getFiles();
            if (archivos == null || archivos.isEmpty()) {
                System.out.println("✅ Carpeta accesible (vacía o sin archivos visibles).");
            } else {
                System.out.println("✅ Carpeta accesible. Archivos encontrados:");
                for (com.google.api.services.drive.model.File f : archivos) {
                    System.out.println(" - " + f.getName() + " (" + f.getId() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ No se pudo acceder a la carpeta de Drive: " + e.getMessage());
        }
    }
}



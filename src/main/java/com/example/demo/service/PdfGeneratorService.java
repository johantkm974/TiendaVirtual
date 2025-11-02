package com.example.demo.service;

// ❌ Se elimina Cloudinary
// import com.cloudinary.Cloudinary;
// import com.cloudinary.utils.ObjectUtils;

import com.example.demo.model.DetalleVenta;
import com.example.demo.model.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// ✅ Imports de Google Drive
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // ✅ Import para @Value
import org.springframework.stereotype.Service;

import java.io.File; // Sigue siendo java.io.File
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections; // ✅ Import para Collections
import java.util.Date;
// ❌ Se elimina Map
// import java.util.Map;

@Service
public class PdfGeneratorService {

    // ❌ Se quita Cloudinary
    // @Autowired
    // private Cloudinary cloudinary;

    // ✅ Se inyecta el servicio de Drive creado en GoogleDriveConfig
    @Autowired
    private Drive driveService;

    // ✅ Se inyecta el ID de la carpeta desde las variables de entorno
    @Value("${GOOGLE_DRIVE_FOLDER_ID}")
    private String googleDriveFolderId;

    public String generarReciboPDF(Venta venta) throws Exception {
        // 📁 Carpeta temporal (Railway solo permite /tmp)
        String carpeta = "/tmp";
        String nombreArchivoLocal = "recibo_venta_" + venta.getId() + ".pdf";
        String rutaCompletaLocal = carpeta + "/" + nombreArchivoLocal;

        // 1. Crear el archivo local (Esto no cambia)
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(rutaCompletaLocal));
        document.open();

        // ... (Todo tu código para rellenar el PDF sigue aquí) ...
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
        
        // ... (Fin del código de rellenar PDF) ...
        
        document.close();

        // 2. ☁️ Subir el PDF generado a Google Drive
        
        // Objeto File de Google Drive (metadatos)
        // ⚠️ Usamos el nombre completo para evitar conflicto con java.io.File
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(nombreArchivoLocal);
        fileMetadata.setParents(Collections.singletonList(googleDriveFolderId));

        // Objeto File de Java (el archivo local)
        java.io.File localFile = new java.io.File(rutaCompletaLocal);

        // Contenido del archivo
        FileContent mediaContent = new FileContent("application/pdf", localFile);

        // Ejecutar la subida
        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink") // Pedimos solo el ID y el link de vista
                .execute();

        // 3. 🌐 Hacer el archivo público (visible para cualquiera con el enlace)
        Permission filePermission = new Permission()
                .setType("anyone")
                .setRole("reader");

        driveService.permissions().create(uploadedFile.getId(), filePermission).execute();

        // 4. 🧹 Limpiar el archivo temporal
        localFile.delete();

        // 5. ✅ Retornar la URL pública del PDF en Google Drive
        return uploadedFile.getWebViewLink();
    }
}


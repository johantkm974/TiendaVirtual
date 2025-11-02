package com.example.demo.service;

import com.example.demo.model.DetalleVenta;
import com.example.demo.model.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PdfGeneratorService {

    public String generarReciboPDF(Venta venta) throws Exception {
        String carpeta = "/tmp";
        String nombreArchivo = carpeta + "/recibo_venta_" + venta.getId() + ".pdf";

        // Crear el PDF
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

        // Subir a File.io
        File pdfFile = new File(nombreArchivo);
        String fileUrl = uploadToFileIo(pdfFile);
        return fileUrl;
    }

    private String uploadToFileIo(File file) throws IOException {
        URL url = new URL("https://file.io/?expires=1d"); // expira en 1 día
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=---ContentBoundary");

        try (OutputStream out = conn.getOutputStream()) {
            out.write(("-----ContentBoundary\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                    "Content-Type: application/pdf\r\n\r\n").getBytes());
            Files.copy(file.toPath(), out);
            out.write("\r\n-----ContentBoundary--\r\n".getBytes());
        }

        // Leer respuesta JSON
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        // Extraer el enlace del JSON
        String json = response.toString();
        int start = json.indexOf("\"link\":\"") + 8;
        int end = json.indexOf("\"", start);
        return json.substring(start, end).replace("\\/", "/");
    }
}


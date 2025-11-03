package com.example.demo.service;

import com.example.demo.model.DetalleVenta;
import com.example.demo.model.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PdfGeneratorService {

    public String generarReciboPDF(Venta venta) throws Exception {
        String carpetaTemporal = "/tmp";
        String nombreArchivo = "recibo_venta_" + venta.getId() + ".pdf";
        String rutaArchivo = carpetaTemporal + "/" + nombreArchivo;

        // 1️⃣ Generar el PDF localmente
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
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

        System.out.println("✅ Recibo PDF generado localmente: " + rutaArchivo);
        return rutaArchivo;
    }
}





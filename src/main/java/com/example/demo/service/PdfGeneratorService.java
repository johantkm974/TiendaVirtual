package com.example.demo.service;

import com.example.demo.model.Venta;
import com.example.demo.model.DetalleVenta;
import org.springframework.stereotype.Service;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    // ✅ Crear carpeta en una ruta absoluta
    String carpeta = System.getProperty("user.dir") + "/recibos_pdf";
    File directorio = new File(carpeta);
    if (!directorio.exists()) {
        boolean creada = directorio.mkdirs();
        if (!creada) {
            throw new RuntimeException("❌ No se pudo crear la carpeta para guardar los recibos PDF");
        }
    }

    String nombreArchivo = carpeta + "/recibo_venta_" + venta.getId() + ".pdf";
    Document document = new Document(PageSize.A4, 50, 50, 70, 50);
    PdfWriter.getInstance(document, new FileOutputStream(nombreArchivo));
    document.open();

        // ==== LOGO ====
        try {
            Image logo = Image.getInstance(getClass().getResource("/static/img/logo.png"));
            logo.scaleToFit(100, 70);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            System.out.println("⚠ No se encontró el logo, continuando sin él...");
        }

        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(11, 102, 35));
        Paragraph titulo = new Paragraph("RECIBO DE VENTA - TIENDA VIRTUAL", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph("\n"));

        Font clienteFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
        document.add(new Paragraph("👤 Cliente: " + venta.getUsuario().getNombre(), clienteFont));
        document.add(new Paragraph("📧 Correo: " + venta.getUsuario().getCorreo(), clienteFont));
        document.add(new Paragraph("💳 Método de Pago: " + venta.getMetodoPago().getNombre(), clienteFont));
        document.add(new Paragraph("📦 Estado del Pago: " + venta.getEstadoPago(), clienteFont));

        if (venta.getFecha() != null) {
            String fecha = venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            document.add(new Paragraph("🕓 Fecha: " + fecha, clienteFont));
        }

        document.add(new Paragraph("\n──────────────────────────────────────────────\n"));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        BaseColor headerColor = new BaseColor(11, 102, 35);
        String[] headers = {"Producto", "Cantidad", "Precio Unit.", "Subtotal"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        Font celdaFont = new Font(Font.FontFamily.HELVETICA, 11);
        for (DetalleVenta d : venta.getDetalles()) {
            table.addCell(new Phrase(d.getProducto().getNombre(), celdaFont));
            table.addCell(new Phrase(String.valueOf(d.getCantidad()), celdaFont));
            table.addCell(new Phrase("S/ " + formatoMoneda.format(d.getPrecioUnitario()), celdaFont));
            table.addCell(new Phrase("S/ " + formatoMoneda.format(d.getSubtotal()), celdaFont));
        }
        document.add(table);

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph total = new Paragraph("TOTAL A PAGAR: S/ " + formatoMoneda.format(venta.getTotal()), totalFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);
        document.close();

        return baos.toByteArray();
    }
}


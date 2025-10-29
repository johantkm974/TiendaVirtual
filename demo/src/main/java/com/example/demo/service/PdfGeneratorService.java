package com.example.demo.service;

import com.example.demo.model.Venta;
import com.example.demo.model.DetalleVenta;
import org.springframework.stereotype.Service;
import java.io.FileOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Service
public class PdfGeneratorService {

    public String generarReciboPDF(Venta venta) throws Exception {
        String carpeta = "recibos_pdf";
        File directorio = new File(carpeta);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String nombreArchivo = carpeta + "/recibo_venta_" + venta.getId() + ".pdf";

        Document document = new Document(PageSize.A4, 50, 50, 70, 50);
        PdfWriter.getInstance(document, new FileOutputStream(nombreArchivo));
        document.open();

        // ==== FORMATEADOR DE MONEDA ====
        DecimalFormat formatoMoneda = new DecimalFormat("0.00");

        // ==== LOGO ====
        try {
            Image logo = Image.getInstance("src/main/resources/static/img/logo.png");
            logo.scaleToFit(100, 70);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            System.out.println("⚠ No se encontró el logo, continuando sin él...");
        }

        // ==== ENCABEZADO ====
        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(11, 102, 35));
        Paragraph titulo = new Paragraph("RECIBO DE VENTA - TIENDA VIRTUAL", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph("\n"));

        // ==== DATOS DE LA EMPRESA ====
        Font empresaFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
        document.add(new Paragraph("📍 Dirección: Huaycán, Ate - Lima, Perú", empresaFont));
        document.add(new Paragraph("📞 Teléfono: (01) 680 - 4484", empresaFont));
        document.add(new Paragraph("✉ Correo: contacto@iestphuaycan.edu.pe", empresaFont));
        document.add(new Paragraph("\n──────────────────────────────────────────────\n"));

        // ==== DATOS DEL CLIENTE ====
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

        // ==== TABLA DE DETALLES ====
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

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

        // ==== TOTAL GENERAL ====
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
        Paragraph total = new Paragraph("TOTAL A PAGAR: S/ " + formatoMoneda.format(venta.getTotal()), totalFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        document.add(new Paragraph("\n──────────────────────────────────────────────\n"));

        // ==== PIE DE PÁGINA PROFESIONAL ====
        Font pieFont = new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC, BaseColor.GRAY);
        document.add(new Paragraph("Gracias por confiar en el Instituto de Educación Superior Tecnológico Público Huaycán.", pieFont));
        document.add(new Paragraph("Tu educación es nuestra prioridad.", pieFont));

        document.add(new Paragraph("\n\nFirma Electrónica:", pieFont));
        document.add(new Paragraph("__________________________", pieFont));
        document.add(new Paragraph("Administrador Johan Vasquez", pieFont));

        document.close();

        System.out.println("✅ PDF generado profesionalmente: " + nombreArchivo);
        return nombreArchivo;
    }
}
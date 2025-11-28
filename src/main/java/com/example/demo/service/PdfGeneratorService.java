package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.model.DetalleVenta;
import com.example.demo.model.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class PdfGeneratorService {

    @Autowired
    private Cloudinary cloudinary;

    public String generarReciboPDF(Venta venta) throws Exception {

        String carpetaTemporal = "/tmp";
        String nombreArchivo = "recibo_venta_" + venta.getId() + ".pdf";
        String rutaArchivo = carpetaTemporal + "/" + nombreArchivo;

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
        document.open();

        /* =======================
             ESTILOS
        ======================= */
        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
        Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

        /* =======================
             TÍTULO
        ======================= */
        Paragraph titulo = new Paragraph("RECIBO DE VENTA", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(Chunk.NEWLINE);

        /* =======================
             INFORMACIÓN GENERAL
        ======================= */
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setSpacingBefore(10);
        info.setSpacingAfter(20);

        info.addCell(celda("ID Venta:", subtituloFont));
        info.addCell(celda(String.valueOf(venta.getId()), normalFont));

        info.addCell(celda("Fecha:", subtituloFont));
        info.addCell(celda(
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
                normalFont
        ));

        info.addCell(celda("Cliente:", subtituloFont));
        info.addCell(celda(venta.getUsuario().getNombre(), normalFont));

        info.addCell(celda("Correo:", subtituloFont));
        info.addCell(celda(venta.getUsuario().getCorreo(), normalFont));

        info.addCell(celda("Método de Pago:", subtituloFont));
        info.addCell(celda(
                venta.getMetodoPago() != null ? venta.getMetodoPago().getNombre() : "N/A",
                normalFont
        ));

        info.addCell(celda("Estado Pago:", subtituloFont));
        info.addCell(celda(venta.getEstadoPago(), normalFont));

        info.addCell(celda("Payment ID:", subtituloFont));
        info.addCell(celda(
                venta.getPaymentId() != null ? venta.getPaymentId() : "N/A",
                normalFont
        ));

        document.add(info);

        /* =======================
             TABLA DE DETALLES
        ======================= */
        Paragraph subtitulo = new Paragraph("Detalles de la Compra", subtituloFont);
        subtitulo.setSpacingAfter(8);
        document.add(subtitulo);

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4, 2, 2, 2});

        tabla.addCell(celdaHeader("Producto"));
        tabla.addCell(celdaHeader("Cantidad"));
        tabla.addCell(celdaHeader("Precio Unit."));
        tabla.addCell(celdaHeader("Subtotal"));

        for (DetalleVenta d : venta.getDetalles()) {
            tabla.addCell(celda(d.getProducto().getNombre(), normalFont));
            tabla.addCell(celda(String.valueOf(d.getCantidad()), normalFont));
            tabla.addCell(celda(String.format("S/ %.2f", d.getPrecio()), normalFont));
            tabla.addCell(celda(String.format("S/ %.2f", d.getSubtotal()), normalFont));
        }

        document.add(tabla);

        /* =======================
             TOTAL
        ======================= */
        Paragraph total = new Paragraph(
                "\nTOTAL A PAGAR:  S/ " + String.format("%.2f", venta.getTotal()),
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)
        );
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        /* =======================
             MENSAJE FINAL
        ======================= */
        Paragraph msg = new Paragraph(
                "\nGracias por tu compra.\nTienda Virtual - Lima Perú",
                normalFont
        );
        msg.setAlignment(Element.ALIGN_CENTER);
        msg.setSpacingBefore(20);
        document.add(msg);

        document.close();

        /* =======================
            SUBIR A CLOUDINARY
        ======================= */
        Map uploadResult = cloudinary.uploader().upload(
                new java.io.File(rutaArchivo),
                ObjectUtils.asMap(
                        "folder", "recibos_tienda_virtual",
                        "resource_type", "auto",
                        "public_id", "recibo_" + venta.getId()
                )
        );

        new java.io.File(rutaArchivo).delete();

        return uploadResult.get("secure_url").toString();
    }

    private PdfPCell celda(String texto, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(texto, font));
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private PdfPCell celdaHeader(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto));
        c.setBackgroundColor(BaseColor.LIGHT_GRAY);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }
}





package com.example.demo.controller;

import com.example.demo.model.Venta;
import com.example.demo.service.EmailService;
import com.example.demo.service.PdfGeneratorService;
import com.example.demo.service.VentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/pago-simulado")
public class PagoSimuladoController {

    private final VentaService ventaService;
    private final PdfGeneratorService pdfService;
    private final EmailService emailService;

    public PagoSimuladoController(VentaService ventaService,
                                  PdfGeneratorService pdfService,
                                  EmailService emailService) {
        this.ventaService = ventaService;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    // 🔹 Marcar venta como pagada con simulación
    @PostMapping("/pagar")
    public ResponseEntity<String> pagarSimulado(@RequestParam Integer ventaId) {
        try {
            Optional<Venta> ventaOpt = ventaService.findById(ventaId);
            if (ventaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("<h2>Venta no encontrada</h2>");
            }

            Venta venta = ventaOpt.get();
            venta.setEstadoPago("APROBADO");
            venta.setPaymentId("SIMULATED_" + System.currentTimeMillis());
            ventaService.save(venta);

            // ✅ Generar PDF local
            String pdfPath = pdfService.generarReciboPDF(venta);
            String nombreArchivo = "recibo_venta_" + venta.getId() + ".pdf";

            // ✅ Enviar correo con PDF adjunto
            if (venta.getUsuario() != null && venta.getUsuario().getCorreo() != null) {
                String correo = venta.getUsuario().getCorreo();
                emailService.enviarReciboAdjunto(correo, pdfPath, nombreArchivo);
                System.out.println("✅ Recibo simulado enviado a: " + correo);
            }

            // ✅ Página HTML de confirmación
            String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta http-equiv="refresh" content="5;url=/index.html">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Pago Simulado Exitoso</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #f0f9ff, #cbebff);
                        height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0;
                    }
                    .card {
                        background: #fff;
                        padding: 50px 70px;
                        border-radius: 16px;
                        box-shadow: 0 8px 20px rgba(0,0,0,0.15);
                        text-align: center;
                    }
                    .checkmark {
                        width: 80px;
                        height: 80px;
                        border-radius: 50%;
                        display: inline-block;
                        border: 4px solid #4CAF50;
                        position: relative;
                        margin-bottom: 25px;
                    }
                    .checkmark::after {
                        content: '';
                        position: absolute;
                        left: 22px;
                        top: 10px;
                        width: 20px;
                        height: 40px;
                        border-right: 5px solid #4CAF50;
                        border-bottom: 5px solid #4CAF50;
                        transform: rotate(45deg);
                    }
                    h1 {
                        color: #333;
                        margin-bottom: 10px;
                    }
                    p {
                        color: #555;
                        font-size: 1.1em;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="checkmark"></div>
                    <h1>¡Pago Simulado Exitoso! 🎉</h1>
                    <p>Tu pago ha sido registrado correctamente.</p>
                    <p>Se envió tu recibo PDF al correo electrónico registrado.</p>
                    <p style="margin-top:20px; color:#888;">Serás redirigido al inicio en unos segundos...</p>
                </div>
            </body>
            </html>
            """;

            return ResponseEntity.ok().body(html);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<h3>Error al procesar el pago simulado: " + e.getMessage() + "</h3>");
        }
    }
}


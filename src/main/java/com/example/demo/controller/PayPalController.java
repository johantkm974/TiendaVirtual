package com.example.demo.controller;

import com.example.demo.model.Venta;
import com.example.demo.service.EmailService;
import com.example.demo.service.PayPalService;
import com.example.demo.service.PdfGeneratorService;
import com.example.demo.service.VentaService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/paypal")

public class PayPalController {

    private final PayPalService payPalService;
    private final VentaService ventaService;
    private final PdfGeneratorService pdfService;
    private final EmailService emailService;

    public PayPalController(
            PayPalService payPalService,
            VentaService ventaService,
            PdfGeneratorService pdfService,
            EmailService emailService
    ) {
        this.payPalService = payPalService;
        this.ventaService = ventaService;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    // 🔹 CREAR PAGO PAYPAL
    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> request) {
        try {
            Double amount = Double.valueOf(request.get("amount").toString());
            String description = request.get("description").toString();
            Integer ventaId = (Integer) request.get("ventaId");

            // Crear pago en PayPal
            Payment payment = payPalService.createPayment(amount, "USD", description);

            // Buscar URL de aprobación
            String approvalUrl = payment.getLinks().stream()
                    .filter(link -> link.getRel().equals("approval_url"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró approval_url"))
                    .getHref();

            // Guardar el ID del pago en la venta si existe
            if (ventaId != null) {
                Optional<Venta> ventaOpt = ventaService.findById(ventaId);
                if (ventaOpt.isPresent()) {
                    Venta venta = ventaOpt.get();
                    venta.setPaymentId(payment.getId());
                    venta.setEstadoPago("PENDIENTE");
                    ventaService.save(venta);
                }
            }

            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("approvalUrl", approvalUrl);
            response.put("paymentId", payment.getId());
            response.put("message", "Pago creado exitosamente");

            return ResponseEntity.ok(response);

        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Error PayPal: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Error: " + e.getMessage()));
        }
    }

    // 🔹 CONFIRMAR PAGO EXITOSO (Genera PDF + Envía correo)
    @GetMapping("/success")
public ResponseEntity<String> paymentSuccess(
        @RequestParam("paymentId") String paymentId,
        @RequestParam("PayerID") String payerId) {

    try {
        // Ejecutar pago en PayPal
        Payment payment = payPalService.executePayment(paymentId, payerId);

        if ("approved".equals(payment.getState())) {
            Optional<Venta> ventaOpt = ventaService.findByPaymentId(paymentId);
            if (ventaOpt.isPresent()) {
                Venta venta = ventaOpt.get();
                venta.setEstadoPago("APROBADO");
                venta.setPaypalPayerId(payerId);
                ventaService.save(venta);

                // ✅ Generar PDF
                System.out.println("🧾 Generando PDF...");
                String pdfPath = pdfService.generarReciboPDF(venta);
                System.out.println("📄 PDF generado en: " + pdfPath);

                // ✅ Enviar correo con el PDF adjunto
                if (venta.getUsuario() != null && venta.getUsuario().getCorreo() != null) {
                    String correo = venta.getUsuario().getCorreo();
                    emailService.enviarCorreoConAdjunto(
                            correo,
                            "Recibo de tu compra #" + venta.getId(),
                            "<h2>¡Gracias por tu compra!</h2><p>Adjuntamos tu recibo en formato PDF.</p>",
                            pdfPath
                    );
                    System.out.println("✅ Correo enviado correctamente a " + correo);
                }
            }

            // ✅ Página de confirmación bonita
            String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta http-equiv="refresh" content="5;url=/index.html">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Compra Exitosa</title>
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
                        animation: fadeIn 1.2s ease;
                    }
                    .checkmark {
                        width: 80px;
                        height: 80px;
                        border-radius: 50%;
                        display: inline-block;
                        border: 4px solid #4CAF50;
                        position: relative;
                        margin-bottom: 25px;
                        animation: pop 0.6s ease-out forwards;
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
                        opacity: 0;
                        animation: draw 0.6s 0.5s forwards ease-out;
                    }
                    h1 {
                        color: #333;
                        margin-bottom: 10px;
                    }
                    p {
                        color: #555;
                        font-size: 1.1em;
                    }
                    @keyframes fadeIn {
                        from { opacity: 0; transform: translateY(20px); }
                        to { opacity: 1; transform: translateY(0); }
                    }
                    @keyframes pop {
                        0% { transform: scale(0.5); opacity: 0; }
                        100% { transform: scale(1); opacity: 1; }
                    }
                    @keyframes draw {
                        to { opacity: 1; }
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="checkmark"></div>
                    <h1>¡Compra Exitosa! 🎉</h1>
                    <p>Tu pago fue procesado correctamente.</p>
                    <p>Se envió tu recibo PDF al correo electrónico registrado.</p>
                    <p style="margin-top:20px; color:#888;">Serás redirigido al inicio en unos segundos...</p>
                </div>
            </body>
            </html>
            """;

            return ResponseEntity.ok().body(html);
        }

        // ❌ Si el pago no fue aprobado
        return ResponseEntity.badRequest()
                .body("<h2>El pago no fue aprobado</h2><p>Por favor, intenta nuevamente.</p>");

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("<h3>Error procesando el pago: " + e.getMessage() + "</h3>");
    }
}

    // 🔹 CANCELAR PAGO
    @GetMapping("/cancel")
    public ResponseEntity<?> paymentCancel(@RequestParam("paymentId") String paymentId) {
        try {
            Optional<Venta> ventaOpt = ventaService.findByPaymentId(paymentId);
            if (ventaOpt.isPresent()) {
                Venta venta = ventaOpt.get();
                venta.setEstadoPago("CANCELADO");
                ventaService.save(venta);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "CANCELLED");
            response.put("paymentId", paymentId);
            response.put("message", "Pago cancelado por el usuario");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

}

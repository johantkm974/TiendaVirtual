package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.EmailService;
import com.example.demo.service.PdfGeneratorService;
import com.example.demo.service.ProductoService;
import com.example.demo.service.VentaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final PdfGeneratorService pdfService;
    private final EmailService emailService;

    public VentaController(
            VentaService ventaService,
            ProductoService productoService,
            PdfGeneratorService pdfService,
            EmailService emailService
    ) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    // ================================
    //   CREAR VENTA + DETALLES
    // ================================
    @PostMapping
    public ResponseEntity<?> crearVenta(@RequestBody Map<String, Object> request) {
        try {
            Integer usuarioId = (Integer) request.get("usuarioId");
            Integer metodoPagoId = (Integer) request.get("metodoPagoId");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detallesRequest =
                    (List<Map<String, Object>>) request.get("detalles");

            List<DetalleVenta> detalles = new ArrayList<>();

            for (Map<String, Object> det : detallesRequest) {
                Integer productoId = (Integer) det.get("productoId");
                Integer cantidad = (Integer) det.get("cantidad");

                Producto producto = productoService.buscarPorId(productoId);
                if (producto == null)
                    throw new RuntimeException("Producto no encontrado: " + productoId);

                DetalleVenta d = new DetalleVenta();
                d.setProducto(producto);
                d.setCantidad(cantidad);
                d.setPrecioUnitario(producto.getPrecio());
                detalles.add(d);
            }

            Venta venta = ventaService.crearVenta(usuarioId, metodoPagoId, detalles);
            return ResponseEntity.status(HttpStatus.CREATED).body(venta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear venta: " + e.getMessage());
        }
    }

    // ================================
    //   PROCESAR VENTA (PDF + CORREO)
    // ================================
    @PostMapping("/procesar")
    public ResponseEntity<Map<String, Object>> procesarVenta(@RequestBody Venta venta) {
        try {
            Venta ventaGuardada = ventaService.save(venta);

            String pdfPath = pdfService.generarReciboPDF(ventaGuardada);

            if (ventaGuardada.getUsuario() != null &&
                    ventaGuardada.getUsuario().getCorreo() != null) {

                emailService.enviarCorreoConAdjunto(
                        ventaGuardada.getUsuario().getCorreo(),
                        "Recibo de tu compra #" + ventaGuardada.getId(),
                        "<h2>Gracias por tu compra</h2><p>Adjunto encontrarás tu recibo en PDF.</p>",
                        pdfPath
                );
            }

            return ResponseEntity.ok(Map.of(
                    "status", "APPROVED",
                    "message", "Pago realizado y correo enviado.",
                    "ventaId", ventaGuardada.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================================
    //   CONFIRMAR PAGO + PDF + EMAIL
    // ================================
    @PostMapping("/confirmar-pago")
    public ResponseEntity<Map<String, Object>> confirmarPago(@RequestBody Map<String, Object> data) {
        try {
            String paymentId = (String) data.get("paymentId");
            Integer ventaId = (Integer) data.get("ventaId");

            Venta venta = ventaService.findById(ventaId).orElse(null);
            if (venta == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Venta no encontrada"));
            }

            // 🔥 Actualizar la venta
            venta.setEstadoPago("APROBADO");
            venta.setPaymentId(paymentId);

            ventaService.save(venta);

            // 🔥 RECARGAR DESDE BD (IMPORTANTE)
            venta = ventaService.findById(ventaId).orElseThrow();

            // 🔥 Generar PDF con datos ACTUALIZADOS
            String pdfPath = pdfService.generarReciboPDF(venta);

            // Enviar correo
            if (venta.getUsuario() != null && venta.getUsuario().getCorreo() != null) {
                emailService.enviarCorreoConAdjunto(
                        venta.getUsuario().getCorreo(),
                        "Comprobante de pago - Orden #" + venta.getId(),
                        "<h2>¡Gracias por tu compra!</h2><p>Adjuntamos tu comprobante.</p>",
                        pdfPath
                );
            }

            return ResponseEntity.ok(Map.of(
                    "status", "APPROVED",
                    "message", "Pago confirmado, PDF generado y correo enviado.",
                    "paymentId", paymentId
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================================
    //   DESCARGAR PDF
    // ================================
    @GetMapping("/{id}/recibo")
    public ResponseEntity<?> descargarRecibo(@PathVariable Integer id) {
        try {
            Optional<Venta> ventaOpt = ventaService.findById(id);
            if (ventaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Venta no encontrada"));
            }

            Venta venta = ventaOpt.get();
            String pdfPath = pdfService.generarReciboPDF(venta);

            File archivo = new File(pdfPath);
            if (!archivo.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "PDF no encontrado"));
            }

            FileSystemResource resource = new FileSystemResource(archivo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(
                    ContentDisposition.inline().filename("recibo_venta_" + id + ".pdf").build()
            );
            headers.setContentType(MediaType.APPLICATION_PDF);

            return ResponseEntity.ok().headers(headers).body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================================
    //   LISTAR TODAS
    // ================================
    @GetMapping
    public ResponseEntity<List<Venta>> obtenerTodas() {
        return ResponseEntity.ok(ventaService.findAll());
    }

    // ================================
    //   OBTENER POR ID
    // ================================
    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Integer id) {
        return ventaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ================================
    //   ELIMINAR
    // ================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarVenta(@PathVariable Integer id) {
        try {
            boolean eliminado = ventaService.eliminarPorId(id);

            if (!eliminado) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Venta no encontrada"));
            }

            return ResponseEntity.ok(Map.of("message", "Venta eliminada correctamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}



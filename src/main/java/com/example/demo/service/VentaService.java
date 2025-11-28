package com.example.demo.service;

import com.example.demo.model.Venta;
import com.example.demo.model.Usuario;
import com.example.demo.model.MetodoPago;
import com.example.demo.model.DetalleVenta;
import com.example.demo.model.Producto;

import com.example.demo.repository.VentaRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.MetodoPagoRepository;
import com.example.demo.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final ProductoRepository productoRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;

    public VentaService(
            VentaRepository ventaRepository,
            UsuarioRepository usuarioRepository,
            MetodoPagoRepository metodoPagoRepository,
            ProductoRepository productoRepository,
            PdfGeneratorService pdfGeneratorService,
            EmailService emailService
    ) {
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.productoRepository = productoRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.emailService = emailService;
    }

    // ============================================================
    // ✅ CREAR UNA VENTA COMPLETA
    // ============================================================
    @Transactional
    public Venta crearVenta(Integer usuarioId, Integer metodoPagoId, List<DetalleVenta> detalles) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        MetodoPago metodoPago = metodoPagoRepository.findById(metodoPagoId)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setMetodoPago(metodoPago);

        double total = 0;

        for (DetalleVenta detalle : detalles) {

            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente: " + producto.getNombre());
            }

            // Reducir stock
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);

            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio() * detalle.getCantidad());
            detalle.setVenta(venta);

            total += detalle.getSubtotal();
        }

        venta.setDetalles(detalles);
        venta.setTotal(total);

        Venta ventaGuardada = ventaRepository.save(venta);

        // Generar y enviar recibo (sin romper venta, se ejecuta aparte)
        try {
            String pdfPath = pdfGeneratorService.generarReciboPDF(ventaGuardada);

            if (usuario.getCorreo() != null && !usuario.getCorreo().isEmpty()) {
                emailService.enviarReciboPorCorreo(usuario.getCorreo(), pdfPath);
            }

        } catch (Exception e) {
            System.err.println("⚠ Error al generar o enviar PDF: " + e.getMessage());
        }

        return ventaGuardada;
    }

    // ============================================================
    // 🔍 Buscar Venta por ID
    // ============================================================
    public Optional<Venta> findById(Integer id) {
        return ventaRepository.findById(id);
    }

    // ============================================================
    // 🔍 Buscar todas las ventas
    // ============================================================
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    // ============================================================
    // 🔍 Buscar por Payment ID (PayPal)
    // ============================================================
    public Optional<Venta> findByPaymentId(String paymentId) {
        return ventaRepository.findByPaymentId(paymentId);
    }

    // ============================================================
    // 🔄 Guardar cambios en venta
    // ============================================================
    public Venta save(Venta venta) {
        return ventaRepository.save(venta);
    }

    // ============================================================
    // ❌ Eliminar venta
    // ============================================================
    public boolean eliminarPorId(Integer id) {
        if (ventaRepository.existsById(id)) {
            ventaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}



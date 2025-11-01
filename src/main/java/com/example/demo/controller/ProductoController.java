package com.example.demo.controller;

import com.example.demo.model.Producto;
import com.example.demo.model.ItemStockDTO;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.service.ProductoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;

    public ProductoController(ProductoService productoService, ProductoRepository productoRepository) {
        this.productoService = productoService;
        this.productoRepository = productoRepository;
    }

    // ✅ Listar todos los productos
    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.listar();
    }

    // ✅ Listar productos por categoría
    @GetMapping("/categoria/{id}")
    public ResponseEntity<List<Producto>> listarPorCategoria(@PathVariable int id) {
        List<Producto> productos = productoService.listarPorCategoria(id);
        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productos);
    }

    // ✅ Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Integer id) {
        Producto producto = productoService.buscarPorId(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    // ✅ Crear producto
    @PostMapping
    public Producto crearProducto(@RequestBody Producto producto) {
        return productoService.guardar(producto);
    }

    // ✅ Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Integer id, @RequestBody Producto producto) {
        Producto existente = productoService.buscarPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecio(producto.getPrecio());
        existente.setStock(producto.getStock());
        existente.setimagen_url(producto.getimagen_url());
        existente.setCategoria(producto.getCategoria());

        return ResponseEntity.ok(productoService.guardar(existente));
    }

    // ✅ Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        Producto producto = productoService.buscarPorId(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Verificar stock antes de realizar compra
    @PostMapping("/verificar-stock")
    public ResponseEntity<Map<String, Object>> verificarStock(@RequestBody List<ItemStockDTO> productos) {
        for (ItemStockDTO item : productos) {
            Producto producto = productoRepository.findById(item.getId()).orElse(null);

            if (producto == null) {
                return ResponseEntity.ok(Map.of(
                        "ok", false,
                        "message", "❌ Producto con ID " + item.getId() + " no encontrado"
                ));
            }

            if (producto.getStock() < item.getCantidad()) {
                return ResponseEntity.ok(Map.of(
                        "ok", false,
                        "message", "🚫 El producto " + producto.getNombre() +
                                " solo tiene " + producto.getStock() + " unidades disponibles."
                ));
            }
        }

        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ✅ Subir imagen (ahora con Cloudinary)
    @PostMapping("/subir-imagen")
    public String subirImagen(@RequestParam("file") MultipartFile file) {
        return productoService.subirImagenCloudinary(file);
    }
}


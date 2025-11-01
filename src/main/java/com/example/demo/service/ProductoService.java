package com.example.demo.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final Cloudinary cloudinary;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;

        // ✅ Configuración segura: toma las credenciales de variables de entorno (Railway o local)
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", System.getenv("CLOUDINARY_CLOUD_NAME"),
                "api_key", System.getenv("CLOUDINARY_API_KEY"),
                "api_secret", System.getenv("CLOUDINARY_API_SECRET"),
                "secure", true
        ));
    }

    // ✅ Listar todos los productos con su categoría
    public List<Producto> listar() {
        return productoRepository.findAllWithCategoria();
    }

    // ✅ Guardar producto
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    // ✅ Buscar producto por ID
    public Producto buscarPorId(int id) {
        return productoRepository.findById(id).orElse(null);
    }

    // ✅ Eliminar producto por ID
    public void eliminar(int id) {
        productoRepository.deleteById(id);
    }

    // ✅ Listar productos por categoría
    public List<Producto> listarPorCategoria(int categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    // ✅ Subir imagen a Cloudinary
    public String subirImagenCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "productos",
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true
            ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al subir imagen a Cloudinary: " + e.getMessage());
        }
    }
}



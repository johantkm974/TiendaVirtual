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

        // ✅ Configuración directa de Cloudinary (puedes moverla a una clase @Configuration luego)
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dsyfifre1",
                "api_key", "463628745861918",
                "api_secret", "3ktzS6ejxiBW2q1kJMh1yAzJI18",
                "secure", true
        ));
    }

    public List<Producto> listar() {
        return productoRepository.findAllWithCategoria();
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto buscarPorId(int id) {
        return productoRepository.findById(id).orElse(null);
    }

    public void eliminar(int id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> listarPorCategoria(int categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    // ✅ Nuevo método: subir imagen a Cloudinary
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


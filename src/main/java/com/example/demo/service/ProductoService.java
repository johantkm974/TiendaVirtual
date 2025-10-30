package com.example.demo.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // ✅ Lista todos los productos con su categoría (join fetch)
    public List<Producto> listar() {
        return productoRepository.findAllWithCategoria();
    }

    // ✅ Guarda o actualiza un producto
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    // ✅ Busca un producto por su ID
    public Producto buscarPorId(int id) {
        return productoRepository.findById(id).orElse(null);
    }

    // ✅ Elimina un producto por su ID
    public void eliminar(int id) {
        productoRepository.deleteById(id);
    }

    // ✅ Corrige el nombre del método para que coincida con el repositorio
    public List<Producto> listarPorCategoria(int categoriaId) {
        return productoRepository.findByCategoria_Id(categoriaId);
    }
}



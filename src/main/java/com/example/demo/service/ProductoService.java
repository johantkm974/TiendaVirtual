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
  
}

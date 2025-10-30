package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto,Integer>{
     List<Producto> findByCategoriaId(int categoriaId);
    
    // 🔥 CONSULTA OPTIMIZADA PARA EVITAR N+1
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria")
    List<Producto> findAllWithCategoria();
    


}

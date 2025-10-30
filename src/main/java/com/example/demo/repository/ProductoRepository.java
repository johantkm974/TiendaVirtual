package com.example.demo.repository;

import com.example.demo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // ✅ Esta es la forma correcta de acceder a categoria.id en JPA
    List<Producto> findByCategoria_Id(int id);

    // 🔥 Consulta optimizada para evitar el problema N+1
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria")
    List<Producto> findAllWithCategoria();
}



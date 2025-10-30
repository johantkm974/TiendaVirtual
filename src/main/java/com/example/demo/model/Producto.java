package com.example.demo.model;

import jakarta.persistence.*;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private int stock;

    // ✅ Aquí está el campo correcto que usas en tu base
    @Lob
    @Column(name = "imagen", columnDefinition = "LONGBLOB")
    @JsonIgnore // <---- EVITA el error 500 al listar productos
    private byte[] imagen;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<DetalleVenta> detalles;

    // ====== Getters / Setters ======
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public byte[] getImagen() { return imagen; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    // Mostrar nombre de la categoría
    @JsonProperty("categoriaNombre")
    public String getCategoriaNombre() {
        return categoria != null ? categoria.getNombre() : "Otros";
    }

    // Mostrar ID de categoría
    @JsonProperty("categoriaId")
    public Integer getCategoriaId() {
        return categoria != null ? categoria.getId() : null;
    }
}


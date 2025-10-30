package com.example.demo.model;

import jakarta.persistence.*;
import java.util.*;
import java.util.Base64;
import com.fasterxml.jackson.annotation.JsonBackReference;
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

    // ✅ Imagen almacenada directamente en la base de datos
    @Lob
    @Column(name = "imagen", columnDefinition = "LONGBLOB")
    private byte[] imagen;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<DetalleVenta> detalles;

    public Producto() {}

    public Producto(int id, String nombre, String descripcion, Double precio, int stock,
                    byte[] imagen, Categoria categoria, List<DetalleVenta> detalles) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.imagen = imagen;
        this.categoria = categoria;
        this.detalles = detalles;
    }

    // Getters y Setters
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

    // 🔥 Nombre de la categoría en JSON
    @JsonProperty("categoriaNombre")
    public String getCategoriaNombre() {
        return categoria != null ? categoria.getNombre() : "Otros";
    }

    // 🔥 Imagen en Base64 para enviar al frontend
    @JsonProperty("imagenBase64")
    public String getImagenBase64() {
        return imagen != null ? Base64.getEncoder().encodeToString(imagen) : null;
    }
}

package com.example.demo.model;

import jakarta.persistence.*;
import java.util.*;

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
    private String imagen_url;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    
    private Categoria categoria;

    @OneToMany(mappedBy = "producto",cascade = CascadeType.ALL)
    @JsonBackReference
    private List<DetalleVenta> detalles;

    public Producto() {
    }

    public Producto(int id, String nombre, String descripcion, Double precio, int stock, String imagen_url,
            Categoria categoria, List<DetalleVenta> detalles) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;

        this.imagen_url = imagen_url;
        this.categoria = categoria;
        this.detalles = detalles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getimagen_url() {
        return imagen_url;
    }

    public void setimagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    // 🔥 MÉTODO PARA OBTENER NOMBRE DE CATEGORÍA EN JSON
    @JsonProperty("categoriaNombre")
    public String getCategoriaNombre() {
        return categoria != null ? categoria.getNombre() : "Otros";
    }

    // 🔥 MÉTODO PARA OBTENER CATEGORÍA COMPLETA EN JSON
    @JsonProperty("categoria")
    public Categoria getCategoriaJson() {
        return categoria;
    }

    


   
    
}
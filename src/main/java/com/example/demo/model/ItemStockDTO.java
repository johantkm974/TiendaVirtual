package com.example.demo.model;

public class ItemStockDTO {

    private Integer id;
    private Integer cantidad;

    public ItemStockDTO() {
    }

    public ItemStockDTO(Integer id, Integer cantidad) {
        this.id = id;
        this.cantidad = cantidad;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}

package com.example.demo.service;

import com.example.demo.model.MetodoPago;
import com.example.demo.repository.MetodoPagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetodoPagoService {
    private final MetodoPagoRepository metodoPagoRepository;

    public MetodoPagoService(MetodoPagoRepository metodoPagoRepository) {
        this.metodoPagoRepository = metodoPagoRepository;
    }

    public Optional<MetodoPago> findById(Integer id) {
        return metodoPagoRepository.findById(id);
    }

    public List<MetodoPago> findAll() {
        return metodoPagoRepository.findAll();
    }
}
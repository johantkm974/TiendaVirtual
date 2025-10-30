package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Usuario findByCorreoAndContrasena(String correo, String contrasena);
    Optional<Usuario>findByCorreo(String correo);
}

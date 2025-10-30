package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    // Listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // Buscar por ID
    public Optional<Usuario> buscarPorId(int id) {
        return usuarioRepository.findById(id);
    }

    // Buscar por correo
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    // 🔹 Registrar nuevo usuario (por defecto rol "Cliente")
    public Usuario registrarUsuario(Usuario usuario) {
        System.out.println("➡ Registrando usuario:");
    System.out.println("Nombre: " + usuario.getNombre());
    System.out.println("Correo: " + usuario.getCorreo());
    System.out.println("Contraseña: " + usuario.getContrasena());
    System.out.println("Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombre() : "NULO"));
        // Si no tiene rol, asignar "Cliente" por defecto
        if (usuario.getRol() == null) {
            Optional<Rol> rolClienteOpt = rolRepository.findByNombre("Cliente");
            Rol rolCliente = rolClienteOpt.orElseThrow(() -> 
                new RuntimeException("El rol 'Cliente' no existe en la base de datos")
            );
            usuario.setRol(rolCliente);
        }
        
        return usuarioRepository.save(usuario);
    }

   // 🔹 LOGIN CORREGIDO - usa findByCorreo y luego verifica contraseña
   public Usuario login(String correo, String contrasena) {
    Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
    
    if (usuarioOpt.isPresent()) {
        Usuario usuario = usuarioOpt.get();
        // Verificar que la contraseña no sea null y coincida
        if (usuario.getContrasena() != null && usuario.getContrasena().equals(contrasena)) {
            return usuario;
        }
    }
    return null;
}
    // Eliminar usuario
    public void eliminarUsuario(int id) {
        usuarioRepository.deleteById(id);
    }
}

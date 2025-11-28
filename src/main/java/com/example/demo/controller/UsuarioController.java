package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Usuario;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")

public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ✅ Listar todos los usuarios (para el administrador)
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarUsuarios();
    }

    // ✅ Buscar por ID
    @GetMapping("/{id}")
    public Optional<Usuario> obtenerPorId(@PathVariable int id) {
        return usuarioService.buscarPorId(id);
    }

    // ✅ Buscar por correo
    @GetMapping("/correo/{correo}")
    public Optional<Usuario> buscarPorCorreo(@PathVariable String correo) {
        return usuarioService.buscarPorCorreo(correo);
    }

    // ✅ Registrar usuario
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error al registrar usuario: " + e.getMessage());
        }
    }

    // ✅ Login (corrigiendo JSON)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario datos) {
        try {
            Usuario usuario = usuarioService.login(datos.getCorreo(), datos.getContrasena());
            if (usuario != null) {
                return ResponseEntity.ok(usuario);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Credenciales incorrectas");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error en el servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ✅ Actualizar usuario
   // ✅ Actualizar usuario (SIN obligar a enviar contraseña)
@PutMapping("/{id}")
public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Usuario usuarioActualizado) {
    Optional<Usuario> usuarioExistente = usuarioService.buscarPorId(id);

    if (usuarioExistente.isPresent()) {
        Usuario usuario = usuarioExistente.get();

        // 🔹 Actualizar nombre y correo siempre
        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setCorreo(usuarioActualizado.getCorreo());

        // 🔹 Solo actualizar contraseña si viene NO vacía
        if (usuarioActualizado.getContrasena() != null &&
            !usuarioActualizado.getContrasena().trim().isEmpty()) 
        {
            usuario.setContrasena(usuarioActualizado.getContrasena());
        }

        // 🔹 Solo actualizar rol si viene uno en el JSON
        if (usuarioActualizado.getRol() != null) {
            usuario.setRol(usuarioActualizado.getRol());
        }

        Usuario actualizado = usuarioService.registrarUsuario(usuario);
        return ResponseEntity.ok(actualizado);
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("Usuario no encontrado");
    }
}


    // ✅ Eliminar usuario
   @DeleteMapping("/{id}")
public ResponseEntity<?> eliminar(@PathVariable int id) {
    try {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}


}



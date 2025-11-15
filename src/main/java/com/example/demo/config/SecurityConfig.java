package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔹 Usar la config de CORS que ya tienes en WebConfig (CorsRegistry)
            .cors(cors -> {}) 
            // 🔹 Desactivar CSRF para poder hacer POST JSON desde tu frontend
            .csrf(csrf -> csrf.disable())
            // 🔹 Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // Permitir registrar usuario sin autenticación
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                // Permitir login sin autenticación previa
                .requestMatchers(HttpMethod.POST, "/api/usuarios/login").permitAll()
                // Permitir recursos públicos (ajusta rutas según tu proyecto)
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/registro.html",
                    "/login.html",
                    "/css/**",
                    "/js/**",
                    "/img/**"
                ).permitAll()
                // Por ahora, todo lo demás también libre (luego puedes cambiar a authenticated())
                .anyRequest().permitAll()
            );

        return http.build();
    }
}

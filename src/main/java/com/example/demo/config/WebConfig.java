package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 🔹 Permitir acceso CORS desde Railway y Localhost
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "https://tiendavirtual-production-88d4.up.railway.app",
                                "http://localhost:8080",
                                "http://127.0.0.1:5500",
                                "http://localhost:5500"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    // 🔹 Permitir servir recursos (imágenes y archivos subidos)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Carpeta para archivos subidos
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // Carpeta de imágenes estáticas dentro del proyecto
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
    }
}

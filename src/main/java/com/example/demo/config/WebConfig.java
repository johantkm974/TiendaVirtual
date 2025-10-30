package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ CORS: habilita acceso tanto desde Railway como desde localhost (para pruebas)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "https://tiendavirtual-production-88d4.up.railway.app",
                                "http://127.0.0.1:5500",
                                "http://localhost:5500"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    // ✅ Mapea las carpetas donde están las imágenes dentro del proyecto
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 📁 Carpeta raíz de uploads (para usuarios, productos, etc.)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // 📁 Carpeta específica de productos
        registry.addResourceHandler("/img/productos/**")
                .addResourceLocations("file:uploads/img/productos/");

        // 📦 Carpeta estática dentro del jar (por si tienes íconos o assets en static/)
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
    }
}

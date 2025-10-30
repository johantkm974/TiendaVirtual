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

   @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🔹 Mapea la carpeta "uploads" (donde se guardan las imágenes)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); 

        // 🔹 También puedes servir imágenes desde la carpeta estática del proyecto
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
    }
}






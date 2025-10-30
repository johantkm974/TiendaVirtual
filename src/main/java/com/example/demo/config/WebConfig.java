package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ CORS
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

    // ✅ Recursos estáticos (productos y otras imágenes)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Si subiste tus imágenes dentro de src/main/resources/static/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");

        // Si están en TiendaVirtual/uploads/img/productos (fuera de resources)
        registry.addResourceHandler("/img/productos/**")
                .addResourceLocations("classpath:/static/img/productos/");

        // Recurso por defecto para íconos
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
    }
}


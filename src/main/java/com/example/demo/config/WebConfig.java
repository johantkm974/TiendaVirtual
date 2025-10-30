package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

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



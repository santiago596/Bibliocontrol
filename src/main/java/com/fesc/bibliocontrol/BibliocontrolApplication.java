package com.fesc.bibliocontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque del Sistema de Inventario para Biblioteca y Préstamo de Libros.
 * Spring Boot inicia el servidor Apache Tomcat embebido en el puerto 8080.
 */
@SpringBootApplication
public class BibliocontrolApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliocontrolApplication.class, args);
    }
}

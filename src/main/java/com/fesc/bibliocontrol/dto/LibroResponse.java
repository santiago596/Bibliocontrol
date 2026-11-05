package com.fesc.bibliocontrol.dto;

import com.fesc.bibliocontrol.persistencia.entidad.Libro;
import java.util.List;

/** Información de un libro, sus ejemplares y su disponibilidad, devuelta a la presentación. */
public record LibroResponse(
        Long id,
        String titulo,
        String autor,
        String categoria,
        String isbn,
        int totalEjemplares,
        long disponibles,
        List<EjemplarResponse> ejemplares) {

    public static LibroResponse desde(Libro libro) {
        List<EjemplarResponse> ejemplares = libro.getEjemplares().stream()
                .map(EjemplarResponse::desde)
                .toList();
        return new LibroResponse(libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getCategoria(),
                libro.getIsbn(), ejemplares.size(), libro.contarDisponibles(), ejemplares);
    }
}

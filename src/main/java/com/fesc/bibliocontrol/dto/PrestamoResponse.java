package com.fesc.bibliocontrol.dto;

import com.fesc.bibliocontrol.persistencia.entidad.Prestamo;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Datos de un préstamo devueltos a la presentación (préstamos activos e historial). */
public record PrestamoResponse(
        Long id,
        String documentoUsuario,
        String nombreUsuario,
        String tipoUsuario,
        String codigoEjemplar,
        String tituloLibro,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        LocalDate fechaDevolucion,
        long plazoDias,
        String estado,
        boolean vencido) {

    public static PrestamoResponse desde(Prestamo prestamo) {
        return new PrestamoResponse(
                prestamo.getId(),
                prestamo.getUsuario().getDocumento(),
                prestamo.getUsuario().getNombre(),
                prestamo.getUsuario().getTipo().name(),
                prestamo.getEjemplar().getCodigo(),
                prestamo.getEjemplar().getLibro().getTitulo(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento(),
                prestamo.getFechaDevolucion(),
                ChronoUnit.DAYS.between(prestamo.getFechaPrestamo(), prestamo.getFechaVencimiento()),
                prestamo.getEstado().name(),
                prestamo.estaVencido(LocalDate.now()));
    }
}

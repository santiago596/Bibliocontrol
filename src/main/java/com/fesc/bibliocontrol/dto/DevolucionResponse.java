package com.fesc.bibliocontrol.dto;

import com.fesc.bibliocontrol.persistencia.entidad.Devolucion;
import com.fesc.bibliocontrol.persistencia.entidad.Prestamo;
import java.time.LocalDate;

/** Resultado de una devolución registrada. */
public record DevolucionResponse(
        Long id,
        Long prestamoId,
        String documentoUsuario,
        String nombreUsuario,
        String codigoEjemplar,
        String tituloLibro,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        LocalDate fechaDevolucion,
        long diasRetraso,
        String condicion,
        String estadoEjemplar,
        String observaciones) {

    public static DevolucionResponse desde(Devolucion devolucion) {
        Prestamo prestamo = devolucion.getPrestamo();
        return new DevolucionResponse(
                devolucion.getId(),
                prestamo.getId(),
                prestamo.getUsuario().getDocumento(),
                prestamo.getUsuario().getNombre(),
                prestamo.getEjemplar().getCodigo(),
                prestamo.getEjemplar().getLibro().getTitulo(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento(),
                devolucion.getFechaDevolucion(),
                devolucion.getDiasRetraso(),
                devolucion.getCondicion().name(),
                prestamo.getEjemplar().getEstado().name(),
                devolucion.getObservaciones());
    }
}

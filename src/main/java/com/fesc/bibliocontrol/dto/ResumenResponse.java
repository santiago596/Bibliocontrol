package com.fesc.bibliocontrol.dto;

/** Indicadores generales del inventario y los préstamos (página de inicio). */
public record ResumenResponse(
        long libros,
        long ejemplares,
        long ejemplaresDisponibles,
        long ejemplaresPrestados,
        long ejemplaresEnReparacion,
        long prestamosActivos,
        long prestamosVencidos,
        long usuariosActivos) {
}

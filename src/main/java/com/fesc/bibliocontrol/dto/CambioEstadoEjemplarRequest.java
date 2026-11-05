package com.fesc.bibliocontrol.dto;

import com.fesc.bibliocontrol.persistencia.entidad.EstadoEjemplar;
import jakarta.validation.constraints.NotNull;

/** Nuevo estado para un ejemplar (DISPONIBLE o EN_REPARACION). */
public record CambioEstadoEjemplarRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoEjemplar estado) {
}

package com.fesc.bibliocontrol.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para registrar nuevos ejemplares de un libro existente. */
public record AgregarEjemplaresRequest(
        @NotNull(message = "La cantidad de ejemplares es obligatoria")
        @Min(value = 1, message = "Debe agregar al menos un ejemplar")
        @Max(value = 50, message = "No se pueden agregar más de 50 ejemplares a la vez")
        Integer cantidad,

        @Size(max = 100, message = "La ubicación no puede superar 100 caracteres")
        String ubicacion) {
}

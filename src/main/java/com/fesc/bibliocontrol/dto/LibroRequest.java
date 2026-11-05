package com.fesc.bibliocontrol.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos enviados por LibroView para registrar un libro con sus ejemplares. */
public record LibroRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar 200 caracteres")
        String titulo,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 150, message = "El autor no puede superar 150 caracteres")
        String autor,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 80, message = "La categoría no puede superar 80 caracteres")
        String categoria,

        @NotBlank(message = "El ISBN es obligatorio")
        @Pattern(regexp = "^[0-9Xx\\- ]{10,17}$", message = "El ISBN solo puede contener dígitos, guiones o X")
        String isbn,

        @NotNull(message = "La cantidad de ejemplares es obligatoria")
        @Min(value = 1, message = "Debe registrar al menos un ejemplar")
        @Max(value = 50, message = "No se pueden registrar más de 50 ejemplares a la vez")
        Integer cantidadEjemplares,

        @Size(max = 100, message = "La ubicación no puede superar 100 caracteres")
        String ubicacion) {
}

package com.fesc.bibliocontrol.dto;

import com.fesc.bibliocontrol.persistencia.entidad.CondicionEjemplar;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos capturados por DevolucionView. */
public record DevolucionRequest(
        @NotBlank(message = "El código del ejemplar es obligatorio")
        String codigoEjemplar,

        @NotNull(message = "La condición del ejemplar es obligatoria")
        CondicionEjemplar condicion,

        @Size(max = 255, message = "Las observaciones no pueden superar 255 caracteres")
        String observaciones) {
}

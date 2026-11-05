package com.fesc.bibliocontrol.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos capturados por PrestamoView: identificación del usuario y código del ejemplar. */
public record PrestamoRequest(
        @NotBlank(message = "La identificación del usuario es obligatoria")
        String documentoUsuario,

        @NotBlank(message = "El código del ejemplar es obligatorio")
        String codigoEjemplar) {
}

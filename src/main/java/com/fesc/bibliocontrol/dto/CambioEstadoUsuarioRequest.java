package com.fesc.bibliocontrol.dto;

import jakarta.validation.constraints.NotNull;

/** Activa o desactiva a un usuario. */
public record CambioEstadoUsuarioRequest(
        @NotNull(message = "Debe indicar si el usuario queda activo")
        Boolean activo) {
}

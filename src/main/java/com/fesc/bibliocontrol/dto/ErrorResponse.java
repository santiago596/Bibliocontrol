package com.fesc.bibliocontrol.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de los mensajes de error que recibe la presentación. */
public record ErrorResponse(int estado, String mensaje, List<String> detalles, LocalDateTime fecha) {

    public static ErrorResponse de(int estado, String mensaje, List<String> detalles) {
        return new ErrorResponse(estado, mensaje, detalles, LocalDateTime.now());
    }
}

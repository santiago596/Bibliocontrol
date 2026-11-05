package com.fesc.bibliocontrol.dto;

import com.fesc.bibliocontrol.persistencia.entidad.Ejemplar;

/** Datos de una copia física de un libro. */
public record EjemplarResponse(String codigo, String estado, String ubicacion) {

    public static EjemplarResponse desde(Ejemplar ejemplar) {
        return new EjemplarResponse(ejemplar.getCodigo(), ejemplar.getEstado().name(), ejemplar.getUbicacion());
    }
}

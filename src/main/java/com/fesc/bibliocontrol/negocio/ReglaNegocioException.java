package com.fesc.bibliocontrol.negocio;

/** Se lanza cuando una operación incumple una regla de negocio de la biblioteca. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

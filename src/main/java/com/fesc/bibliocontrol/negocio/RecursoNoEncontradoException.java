package com.fesc.bibliocontrol.negocio;

/** Se lanza cuando no existe el usuario, libro o ejemplar solicitado. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

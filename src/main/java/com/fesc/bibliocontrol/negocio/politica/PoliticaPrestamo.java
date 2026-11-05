package com.fesc.bibliocontrol.negocio.politica;

import com.fesc.bibliocontrol.persistencia.entidad.TipoUsuario;

/**
 * Patrón Strategy (sección 9 del documento): interfaz común para las políticas de préstamo.
 * Cada implementación encapsula la regla de plazo de un tipo de usuario.
 */
public interface PoliticaPrestamo {

    /** Indica si la política corresponde al tipo de usuario recibido. */
    boolean aplicaA(TipoUsuario tipoUsuario);

    /** Número de días que dura el préstamo. */
    int calcularPlazo();
}

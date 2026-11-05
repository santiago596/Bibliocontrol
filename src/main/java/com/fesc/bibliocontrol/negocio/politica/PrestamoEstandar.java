package com.fesc.bibliocontrol.negocio.politica;

import com.fesc.bibliocontrol.persistencia.entidad.TipoUsuario;
import org.springframework.stereotype.Component;

/** Estrategia para estudiantes. Plazo ilustrativo: se ajusta según el reglamento de la biblioteca. */
@Component
public class PrestamoEstandar implements PoliticaPrestamo {

    private static final int PLAZO_DIAS = 8;

    @Override
    public boolean aplicaA(TipoUsuario tipoUsuario) {
        return tipoUsuario == TipoUsuario.ESTUDIANTE;
    }

    @Override
    public int calcularPlazo() {
        return PLAZO_DIAS;
    }
}

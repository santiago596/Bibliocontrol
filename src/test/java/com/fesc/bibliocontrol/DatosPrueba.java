package com.fesc.bibliocontrol;

import com.fesc.bibliocontrol.dto.LibroRequest;
import com.fesc.bibliocontrol.negocio.InventarioService;
import com.fesc.bibliocontrol.persistencia.entidad.TipoUsuario;
import com.fesc.bibliocontrol.persistencia.entidad.Usuario;
import com.fesc.bibliocontrol.persistencia.repositorio.UsuarioRepository;

/** Datos comunes para las pruebas. */
final class DatosPrueba {

    static final String ESTUDIANTE = "1090000001";
    static final String DOCENTE = "1090000002";
    static final String INACTIVO = "1090000003";
    static final String ISBN = "9789584276971";
    static final String EJEMPLAR_1 = ISBN + "-1";
    static final String EJEMPLAR_2 = ISBN + "-2";

    private DatosPrueba() {
    }

    static void crearUsuarios(UsuarioRepository repositorio) {
        repositorio.save(new Usuario(ESTUDIANTE, "Laura Gómez", "laura@fesc.edu.co", null, TipoUsuario.ESTUDIANTE, true));
        repositorio.save(new Usuario(DOCENTE, "Carlos Pérez", "carlos@fesc.edu.co", null, TipoUsuario.DOCENTE, true));
        repositorio.save(new Usuario(INACTIVO, "Ana Ruiz", "ana@fesc.edu.co", null, TipoUsuario.ESTUDIANTE, false));
    }

    static void crearLibro(InventarioService inventarioService) {
        inventarioService.registrarLibro(new LibroRequest("El amor en los tiempos del cólera",
                "Gabriel García Márquez", "Novela", "978-958-42-7697-1", 2, "Estantería B-1"));
    }
}

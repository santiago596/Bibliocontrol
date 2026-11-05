package com.fesc.bibliocontrol;

import static com.fesc.bibliocontrol.DatosPrueba.DOCENTE;
import static com.fesc.bibliocontrol.DatosPrueba.EJEMPLAR_1;
import static com.fesc.bibliocontrol.DatosPrueba.ESTUDIANTE;
import static com.fesc.bibliocontrol.DatosPrueba.INACTIVO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fesc.bibliocontrol.negocio.InventarioService;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoEjemplar;
import com.fesc.bibliocontrol.persistencia.repositorio.PrestamoRepository;
import com.fesc.bibliocontrol.persistencia.repositorio.UsuarioRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Funcionalidad: Realizar préstamo de libro (caso testigo). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RealizarPrestamoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @BeforeEach
    void prepararDatos() {
        DatosPrueba.crearUsuarios(usuarioRepository);
        DatosPrueba.crearLibro(inventarioService);
    }

    private ResultActions solicitarPrestamo(String documento, String codigoEjemplar) throws Exception {
        String cuerpo = "{\"documentoUsuario\":\"" + documento + "\",\"codigoEjemplar\":\"" + codigoEjemplar + "\"}";
        return mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    @DisplayName("Caso exitoso: estudiante activo y ejemplar disponible -> préstamo de 8 días y ejemplar PRESTADO")
    void prestamoExitosoEstudiante() throws Exception {
        solicitarPrestamo(ESTUDIANTE, EJEMPLAR_1)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreUsuario").value("Laura Gómez"))
                .andExpect(jsonPath("$.tituloLibro").value("El amor en los tiempos del cólera"))
                .andExpect(jsonPath("$.plazoDias").value(8))
                .andExpect(jsonPath("$.fechaVencimiento").value(LocalDate.now().plusDays(8).toString()))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.vencido").value(false));

        assertThat(inventarioService.consultarDisponibilidad(EJEMPLAR_1)).isEqualTo(EstadoEjemplar.PRESTADO);
        assertThat(prestamoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Strategy: a un docente se le aplica la política de 15 días")
    void prestamoDocenteUsaPoliticaDocente() throws Exception {
        solicitarPrestamo(DOCENTE, EJEMPLAR_1)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoUsuario").value("DOCENTE"))
                .andExpect(jsonPath("$.plazoDias").value(15));
    }

    @Test
    @DisplayName("Regla de negocio: no se presta un ejemplar que ya está prestado")
    void rechazaEjemplarNoDisponible() throws Exception {
        solicitarPrestamo(ESTUDIANTE, EJEMPLAR_1).andExpect(status().isCreated());

        solicitarPrestamo(DOCENTE, EJEMPLAR_1)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje")
                        .value("El ejemplar " + EJEMPLAR_1 + " no está disponible (estado actual: PRESTADO)."));
        assertThat(prestamoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regla de negocio: no se presta un ejemplar en reparación")
    void rechazaEjemplarEnReparacion() throws Exception {
        inventarioService.cambiarEstadoManual(EJEMPLAR_1, EstadoEjemplar.EN_REPARACION);

        solicitarPrestamo(ESTUDIANTE, EJEMPLAR_1)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje")
                        .value("El ejemplar " + EJEMPLAR_1 + " no está disponible (estado actual: EN_REPARACION)."));
    }

    @Test
    @DisplayName("Regla de negocio: un usuario inactivo no puede recibir préstamos y el ejemplar sigue disponible")
    void rechazaUsuarioInactivo() throws Exception {
        solicitarPrestamo(INACTIVO, EJEMPLAR_1)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("El usuario Ana Ruiz está inactivo y no puede recibir préstamos."));

        assertThat(inventarioService.consultarDisponibilidad(EJEMPLAR_1)).isEqualTo(EstadoEjemplar.DISPONIBLE);
        assertThat(prestamoRepository.count()).isZero();
    }

    @Test
    @DisplayName("Manejo de errores: usuario no registrado")
    void rechazaUsuarioInexistente() throws Exception {
        solicitarPrestamo("999", EJEMPLAR_1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe un usuario registrado con la identificación 999."));
    }

    @Test
    @DisplayName("Manejo de errores: ejemplar inexistente")
    void rechazaEjemplarInexistente() throws Exception {
        solicitarPrestamo(ESTUDIANTE, "NO-EXISTE")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe un ejemplar con el código NO-EXISTE."));
    }

    @Test
    @DisplayName("Validación: datos de entrada vacíos")
    void rechazaDatosVacios() throws Exception {
        solicitarPrestamo("", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(2));
    }
}

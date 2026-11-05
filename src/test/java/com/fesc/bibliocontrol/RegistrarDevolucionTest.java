package com.fesc.bibliocontrol;

import static com.fesc.bibliocontrol.DatosPrueba.EJEMPLAR_1;
import static com.fesc.bibliocontrol.DatosPrueba.EJEMPLAR_2;
import static com.fesc.bibliocontrol.DatosPrueba.ESTUDIANTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fesc.bibliocontrol.dto.PrestamoRequest;
import com.fesc.bibliocontrol.negocio.InventarioService;
import com.fesc.bibliocontrol.negocio.PrestamoService;
import com.fesc.bibliocontrol.persistencia.entidad.Ejemplar;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoEjemplar;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoPrestamo;
import com.fesc.bibliocontrol.persistencia.entidad.Prestamo;
import com.fesc.bibliocontrol.persistencia.repositorio.DevolucionRepository;
import com.fesc.bibliocontrol.persistencia.repositorio.EjemplarRepository;
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

/** Gestión de Devoluciones: registrar la devolución y actualizar el estado del ejemplar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrarDevolucionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EjemplarRepository ejemplarRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private DevolucionRepository devolucionRepository;

    @BeforeEach
    void prepararDatos() {
        DatosPrueba.crearUsuarios(usuarioRepository);
        DatosPrueba.crearLibro(inventarioService);
    }

    private ResultActions devolver(String codigo, String condicion) throws Exception {
        String cuerpo = "{\"codigoEjemplar\":\"" + codigo + "\",\"condicion\":\"" + condicion
                + "\",\"observaciones\":\"Sin novedad\"}";
        return mockMvc.perform(post("/api/devoluciones").contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    @DisplayName("Caso exitoso: devolución a tiempo -> préstamo DEVUELTO y ejemplar DISPONIBLE")
    void devolucionExitosa() throws Exception {
        prestamoService.registrarPrestamo(new PrestamoRequest(ESTUDIANTE, EJEMPLAR_1));

        devolver(EJEMPLAR_1, "BUENO")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreUsuario").value("Laura Gómez"))
                .andExpect(jsonPath("$.diasRetraso").value(0))
                .andExpect(jsonPath("$.estadoEjemplar").value("DISPONIBLE"))
                .andExpect(jsonPath("$.fechaDevolucion").value(LocalDate.now().toString()));

        assertThat(inventarioService.consultarDisponibilidad(EJEMPLAR_1)).isEqualTo(EstadoEjemplar.DISPONIBLE);
        assertThat(prestamoRepository.findAll().get(0).getEstado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(devolucionRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regla de negocio: un ejemplar devuelto dañado pasa a EN_REPARACION")
    void devolucionEjemplarDanado() throws Exception {
        prestamoService.registrarPrestamo(new PrestamoRequest(ESTUDIANTE, EJEMPLAR_1));

        devolver(EJEMPLAR_1, "DANADO")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.condicion").value("DANADO"))
                .andExpect(jsonPath("$.estadoEjemplar").value("EN_REPARACION"));
    }

    @Test
    @DisplayName("Regla de negocio: se calculan los días de retraso de una devolución tardía")
    void devolucionConRetraso() throws Exception {
        Ejemplar ejemplar = ejemplarRepository.findByCodigo(EJEMPLAR_2).orElseThrow();
        ejemplar.cambiarEstado(EstadoEjemplar.PRESTADO);
        LocalDate hoy = LocalDate.now();
        prestamoRepository.save(new Prestamo(usuarioRepository.findByDocumento(ESTUDIANTE).orElseThrow(),
                ejemplar, hoy.minusDays(20), hoy.minusDays(12)));

        devolver(EJEMPLAR_2, "BUENO")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diasRetraso").value(12));
    }

    @Test
    @DisplayName("Regla de negocio: solo se registra la devolución de un préstamo activo")
    void rechazaEjemplarSinPrestamoActivo() throws Exception {
        devolver(EJEMPLAR_1, "BUENO")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje")
                        .value("El ejemplar " + EJEMPLAR_1 + " no tiene un préstamo pendiente de devolución."));
    }

    @Test
    @DisplayName("Regla de negocio: no se puede devolver dos veces el mismo préstamo")
    void rechazaDobleDevolucion() throws Exception {
        prestamoService.registrarPrestamo(new PrestamoRequest(ESTUDIANTE, EJEMPLAR_1));
        devolver(EJEMPLAR_1, "BUENO").andExpect(status().isCreated());

        devolver(EJEMPLAR_1, "BUENO").andExpect(status().isUnprocessableEntity());
        assertThat(devolucionRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Manejo de errores: ejemplar inexistente y condición inválida")
    void rechazaDatosInvalidos() throws Exception {
        devolver("NO-EXISTE", "BUENO").andExpect(status().isNotFound());
        devolver(EJEMPLAR_1, "PERDIDO").andExpect(status().isBadRequest());
    }
}

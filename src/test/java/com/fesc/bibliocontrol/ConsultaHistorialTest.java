package com.fesc.bibliocontrol;

import static com.fesc.bibliocontrol.DatosPrueba.DOCENTE;
import static com.fesc.bibliocontrol.DatosPrueba.EJEMPLAR_1;
import static com.fesc.bibliocontrol.DatosPrueba.EJEMPLAR_2;
import static com.fesc.bibliocontrol.DatosPrueba.ESTUDIANTE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fesc.bibliocontrol.dto.DevolucionRequest;
import com.fesc.bibliocontrol.dto.PrestamoRequest;
import com.fesc.bibliocontrol.negocio.DevolucionService;
import com.fesc.bibliocontrol.negocio.InventarioService;
import com.fesc.bibliocontrol.negocio.PrestamoService;
import com.fesc.bibliocontrol.persistencia.entidad.CondicionEjemplar;
import com.fesc.bibliocontrol.persistencia.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Consultas e Historial: historial de préstamos e indicadores. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConsultaHistorialTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private DevolucionService devolucionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void prepararDatos() {
        DatosPrueba.crearUsuarios(usuarioRepository);
        DatosPrueba.crearLibro(inventarioService);
        // Laura: un préstamo devuelto y uno activo. Carlos: ninguno.
        prestamoService.registrarPrestamo(new PrestamoRequest(ESTUDIANTE, EJEMPLAR_1));
        devolucionService.registrarDevolucion(new DevolucionRequest(EJEMPLAR_1, CondicionEjemplar.BUENO, null));
        prestamoService.registrarPrestamo(new PrestamoRequest(ESTUDIANTE, EJEMPLAR_2));
    }

    @Test
    @DisplayName("Historial de un usuario: incluye préstamos activos y devueltos")
    void historialPorUsuario() throws Exception {
        mockMvc.perform(get("/api/historial").param("documento", ESTUDIANTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$[1].estado").value("DEVUELTO"));

        mockMvc.perform(get("/api/historial").param("documento", DOCENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Historial filtrado por estado")
    void historialPorEstado() throws Exception {
        mockMvc.perform(get("/api/historial").param("estado", "DEVUELTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigoEjemplar").value(EJEMPLAR_1));
    }

    @Test
    @DisplayName("Manejo de errores: usuario inexistente y filtro inválido")
    void historialConErrores() throws Exception {
        mockMvc.perform(get("/api/historial").param("documento", "999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/historial").param("estado", "PERDIDO"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Resumen: indicadores de inventario y préstamos")
    void resumen() throws Exception {
        mockMvc.perform(get("/api/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libros").value(1))
                .andExpect(jsonPath("$.ejemplares").value(2))
                .andExpect(jsonPath("$.ejemplaresDisponibles").value(1))
                .andExpect(jsonPath("$.ejemplaresPrestados").value(1))
                .andExpect(jsonPath("$.prestamosActivos").value(1))
                .andExpect(jsonPath("$.prestamosVencidos").value(0))
                .andExpect(jsonPath("$.usuariosActivos").value(2));
    }
}

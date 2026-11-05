package com.fesc.bibliocontrol;

import static com.fesc.bibliocontrol.DatosPrueba.EJEMPLAR_1;
import static com.fesc.bibliocontrol.DatosPrueba.ESTUDIANTE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fesc.bibliocontrol.dto.PrestamoRequest;
import com.fesc.bibliocontrol.negocio.InventarioService;
import com.fesc.bibliocontrol.negocio.PrestamoService;
import com.fesc.bibliocontrol.persistencia.repositorio.LibroRepository;
import com.fesc.bibliocontrol.persistencia.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Gestión de Inventario: modificar libros, agregar ejemplares y cambiar su estado. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GestionInventarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long idLibro;

    @BeforeEach
    void prepararDatos() {
        DatosPrueba.crearUsuarios(usuarioRepository);
        DatosPrueba.crearLibro(inventarioService);
        idLibro = libroRepository.findAll().get(0).getId();
    }

    @Test
    @DisplayName("Modifica título, autor y categoría de un libro")
    void modificarLibro() throws Exception {
        mockMvc.perform(put("/api/libros/" + idLibro).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Nuevo título\",\"autor\":\"G. G. Márquez\",\"categoria\":\"Clásicos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Nuevo título"))
                .andExpect(jsonPath("$.categoria").value("Clásicos"));
    }

    @Test
    @DisplayName("Manejo de errores: modificar un libro que no existe")
    void modificarLibroInexistente() throws Exception {
        mockMvc.perform(put("/api/libros/9999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"X\",\"autor\":\"Y\",\"categoria\":\"Z\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el libro con id 9999."));
    }

    @Test
    @DisplayName("Agrega ejemplares con código consecutivo")
    void agregarEjemplares() throws Exception {
        mockMvc.perform(post("/api/libros/" + idLibro + "/ejemplares").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":2,\"ubicacion\":\"Estantería B-2\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalEjemplares").value(4))
                .andExpect(jsonPath("$.ejemplares[3].codigo").value(DatosPrueba.ISBN + "-4"));
    }

    @Test
    @DisplayName("Cambia un ejemplar disponible a EN_REPARACION")
    void enviarEjemplarAReparacion() throws Exception {
        mockMvc.perform(patch("/api/ejemplares/" + EJEMPLAR_1 + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_REPARACION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REPARACION"));
    }

    @Test
    @DisplayName("Regla de negocio: no se cambia manualmente el estado de un ejemplar prestado")
    void rechazaCambioDeEjemplarPrestado() throws Exception {
        prestamoService.registrarPrestamo(new PrestamoRequest(ESTUDIANTE, EJEMPLAR_1));

        mockMvc.perform(patch("/api/ejemplares/" + EJEMPLAR_1 + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"DISPONIBLE\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("El ejemplar " + EJEMPLAR_1
                        + " está prestado; su estado cambiará al registrar la devolución."));
    }

    @Test
    @DisplayName("Regla de negocio: el estado PRESTADO no se asigna manualmente")
    void rechazaAsignarPrestadoManualmente() throws Exception {
        mockMvc.perform(patch("/api/ejemplares/" + EJEMPLAR_1 + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"PRESTADO\"}"))
                .andExpect(status().isUnprocessableEntity());
    }
}

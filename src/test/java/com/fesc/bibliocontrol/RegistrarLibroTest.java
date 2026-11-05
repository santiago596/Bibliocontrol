package com.fesc.bibliocontrol;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Funcionalidad 1: Registrar y consultar libros.
 * Recorre el flujo completo: LibroController -> InventarioService -> LibroRepository -> base de datos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrarLibroTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String LIBRO_VALIDO = """
            {"titulo":"Cien años de soledad","autor":"Gabriel García Márquez","categoria":"Novela",
             "isbn":"978-0-307-47472-8","cantidadEjemplares":2,"ubicacion":"Estantería A-3"}
            """;

    @Test
    @DisplayName("Caso exitoso: registra el libro con sus ejemplares disponibles y aparece en la consulta")
    void registrarLibroExitoso() throws Exception {
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(LIBRO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("9780307474728"))
                .andExpect(jsonPath("$.totalEjemplares").value(2))
                .andExpect(jsonPath("$.disponibles").value(2))
                .andExpect(jsonPath("$.ejemplares[0].codigo").value("9780307474728-1"))
                .andExpect(jsonPath("$.ejemplares[0].estado").value("DISPONIBLE"));

        mockMvc.perform(get("/api/libros").param("criterio", "soledad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", hasItem("Cien años de soledad")));
    }

    @Test
    @DisplayName("Regla de negocio: no permite registrar dos libros con el mismo ISBN")
    void rechazaIsbnDuplicado() throws Exception {
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(LIBRO_VALIDO))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(LIBRO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un libro registrado con el ISBN 9780307474728."));
    }

    @Test
    @DisplayName("Validación: rechaza un libro con datos obligatorios faltantes")
    void rechazaDatosObligatoriosFaltantes() throws Exception {
        String incompleto = """
                {"titulo":"","autor":"Autor","categoria":"","isbn":"9780307474728","cantidadEjemplares":0}
                """;
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(incompleto))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("El título es obligatorio")))
                .andExpect(jsonPath("$.detalles", hasItem("La categoría es obligatoria")))
                .andExpect(jsonPath("$.detalles", hasItem("Debe registrar al menos un ejemplar")));
    }

    @Test
    @DisplayName("Regla de negocio: el ISBN debe tener 10 o 13 dígitos")
    void rechazaIsbnConLongitudInvalida() throws Exception {
        String isbnInvalido = """
                {"titulo":"Libro","autor":"Autor","categoria":"Ensayo","isbn":"123-456-7890-12","cantidadEjemplares":1}
                """;
        mockMvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(isbnInvalido))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("El ISBN debe tener 10 o 13 dígitos (se recibieron 12)."));
    }
}

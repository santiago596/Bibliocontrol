package com.fesc.bibliocontrol;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Gestión de Usuarios: registrar, consultar, modificar, activar y desactivar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GestionUsuariosTest {

    private static final String USUARIO = """
            {"documento":"1090123456","nombre":"Sofía Ramírez","email":"sofia@fesc.edu.co",
             "telefono":"3001234567","tipo":"ESTUDIANTE"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long registrar() throws Exception {
        String respuesta = mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(USUARIO))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(respuesta);
        return json.get("id").asLong();
    }

    @Test
    @DisplayName("Caso exitoso: registra un usuario activo y aparece en la consulta")
    void registrarUsuario() throws Exception {
        registrar();
        mockMvc.perform(get("/api/usuarios").param("criterio", "sofía"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documento").value("1090123456"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @DisplayName("Regla de negocio: la identificación no puede repetirse")
    void rechazaDocumentoDuplicado() throws Exception {
        registrar();
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(USUARIO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un usuario registrado con la identificación 1090123456."));
    }

    @Test
    @DisplayName("Validación: identificación, nombre, correo y tipo")
    void rechazaDatosInvalidos() throws Exception {
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"12a\",\"nombre\":\"\",\"email\":\"correo-malo\",\"tipo\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("La identificación debe tener entre 5 y 15 dígitos")))
                .andExpect(jsonPath("$.detalles", hasItem("El nombre es obligatorio")))
                .andExpect(jsonPath("$.detalles", hasItem("El correo electrónico no es válido")))
                .andExpect(jsonPath("$.detalles", hasItem("El tipo de usuario es obligatorio")));
    }

    @Test
    @DisplayName("Modifica los datos de un usuario")
    void modificarUsuario() throws Exception {
        long id = registrar();
        mockMvc.perform(put("/api/usuarios/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"1090123456\",\"nombre\":\"Sofía Ramírez López\",\"tipo\":\"DOCENTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sofía Ramírez López"))
                .andExpect(jsonPath("$.tipo").value("DOCENTE"));
    }

    @Test
    @DisplayName("Desactiva un usuario y luego no puede recibir préstamos")
    void desactivarUsuario() throws Exception {
        long id = registrar();
        mockMvc.perform(patch("/api/usuarios/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentoUsuario\":\"1090123456\",\"codigoEjemplar\":\"X-1\"}"))
                .andExpect(status().isUnprocessableEntity());
    }
}

package com.fesc.bibliocontrol.acceso;

import com.fesc.bibliocontrol.dto.AgregarEjemplaresRequest;
import com.fesc.bibliocontrol.dto.LibroRequest;
import com.fesc.bibliocontrol.dto.LibroResponse;
import com.fesc.bibliocontrol.dto.ModificarLibroRequest;
import com.fesc.bibliocontrol.negocio.InventarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Acceso al negocio del componente Gestión de Inventario (libros).
 * Recibe las solicitudes HTTP de LibroView y las delega a InventarioService.
 */
@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final InventarioService inventarioService;

    public LibroController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /** POST /api/libros: registra un libro con sus ejemplares. */
    @PostMapping
    public ResponseEntity<LibroResponse> registrarLibro(@Valid @RequestBody LibroRequest solicitud) {
        LibroResponse libro = inventarioService.registrarLibro(solicitud);
        return ResponseEntity.created(URI.create("/api/libros/" + libro.id())).body(libro);
    }

    /** GET /api/libros?criterio=...: consulta libros y su disponibilidad. */
    @GetMapping
    public List<LibroResponse> consultarLibros(@RequestParam(required = false) String criterio) {
        return inventarioService.consultarLibros(criterio);
    }

    /** PUT /api/libros/{id}: modifica la información de un libro. */
    @PutMapping("/{id}")
    public LibroResponse modificarLibro(@PathVariable Long id, @Valid @RequestBody ModificarLibroRequest solicitud) {
        return inventarioService.modificarLibro(id, solicitud);
    }

    /** POST /api/libros/{id}/ejemplares: registra nuevos ejemplares de un libro. */
    @PostMapping("/{id}/ejemplares")
    public ResponseEntity<LibroResponse> agregarEjemplares(@PathVariable Long id,
                                                           @Valid @RequestBody AgregarEjemplaresRequest solicitud) {
        LibroResponse libro = inventarioService.agregarEjemplares(id, solicitud);
        return ResponseEntity.created(URI.create("/api/libros/" + libro.id())).body(libro);
    }
}

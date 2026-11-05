package com.fesc.bibliocontrol.acceso;

import com.fesc.bibliocontrol.dto.PrestamoRequest;
import com.fesc.bibliocontrol.dto.PrestamoResponse;
import com.fesc.bibliocontrol.negocio.PrestamoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Acceso al negocio de la funcionalidad "Realizar préstamo de libro".
 * Recibe la solicitud de PrestamoView, la delega a PrestamoService y devuelve el resultado.
 * No aplica reglas de negocio: solo dirige la solicitud y prepara la respuesta HTTP.
 */
@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    /** Endpoint POST /api/prestamos: registra un préstamo. */
    @PostMapping
    public ResponseEntity<PrestamoResponse> registrarPrestamo(@Valid @RequestBody PrestamoRequest solicitud) {
        PrestamoResponse prestamo = prestamoService.registrarPrestamo(solicitud);
        return ResponseEntity.created(URI.create("/api/prestamos/" + prestamo.id())).body(prestamo);
    }

    /** Endpoint GET /api/prestamos: lista los préstamos activos. */
    @GetMapping
    public List<PrestamoResponse> consultarPrestamosActivos() {
        return prestamoService.consultarPrestamosActivos();
    }
}

package com.fesc.bibliocontrol.acceso;

import com.fesc.bibliocontrol.dto.DevolucionRequest;
import com.fesc.bibliocontrol.dto.DevolucionResponse;
import com.fesc.bibliocontrol.negocio.DevolucionService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Acceso al negocio del componente Gestión de Devoluciones. */
@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    /** POST /api/devoluciones: registra la devolución de un ejemplar prestado. */
    @PostMapping
    public ResponseEntity<DevolucionResponse> registrarDevolucion(@Valid @RequestBody DevolucionRequest solicitud) {
        DevolucionResponse devolucion = devolucionService.registrarDevolucion(solicitud);
        return ResponseEntity.created(URI.create("/api/devoluciones/" + devolucion.id())).body(devolucion);
    }
}

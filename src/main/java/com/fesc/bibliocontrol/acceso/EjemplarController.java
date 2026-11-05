package com.fesc.bibliocontrol.acceso;

import com.fesc.bibliocontrol.dto.CambioEstadoEjemplarRequest;
import com.fesc.bibliocontrol.dto.EjemplarResponse;
import com.fesc.bibliocontrol.negocio.InventarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Acceso al negocio del componente Gestión de Inventario (ejemplares). */
@RestController
@RequestMapping("/api/ejemplares")
public class EjemplarController {

    private final InventarioService inventarioService;

    public EjemplarController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /** PATCH /api/ejemplares/{codigo}/estado: cambia el estado de un ejemplar (DISPONIBLE / EN_REPARACION). */
    @PatchMapping("/{codigo}/estado")
    public EjemplarResponse cambiarEstado(@PathVariable String codigo,
                                          @Valid @RequestBody CambioEstadoEjemplarRequest solicitud) {
        return inventarioService.cambiarEstadoManual(codigo, solicitud.estado());
    }
}

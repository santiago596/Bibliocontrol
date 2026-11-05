package com.fesc.bibliocontrol.acceso;

import com.fesc.bibliocontrol.dto.PrestamoResponse;
import com.fesc.bibliocontrol.dto.ResumenResponse;
import com.fesc.bibliocontrol.negocio.ConsultaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Acceso al negocio del componente Consultas e Historial. */
@RestController
@RequestMapping("/api")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    /** GET /api/historial?documento=...&amp;estado=...: historial de préstamos. */
    @GetMapping("/historial")
    public List<PrestamoResponse> consultarHistorial(@RequestParam(required = false) String documento,
                                                     @RequestParam(required = false) String estado) {
        return consultaService.consultarHistorial(documento, estado);
    }

    /** GET /api/resumen: indicadores del inventario y los préstamos. */
    @GetMapping("/resumen")
    public ResumenResponse consultarResumen() {
        return consultaService.consultarResumen();
    }
}

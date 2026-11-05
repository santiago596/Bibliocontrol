package com.fesc.bibliocontrol.negocio;

import com.fesc.bibliocontrol.dto.DevolucionRequest;
import com.fesc.bibliocontrol.dto.DevolucionResponse;
import com.fesc.bibliocontrol.persistencia.entidad.CondicionEjemplar;
import com.fesc.bibliocontrol.persistencia.entidad.Devolucion;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoEjemplar;
import com.fesc.bibliocontrol.persistencia.entidad.Prestamo;
import com.fesc.bibliocontrol.persistencia.repositorio.DevolucionRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del negocio del componente Gestión de Devoluciones.
 * Depende de Gestión de Préstamos (identificar el préstamo) y de Gestión de Inventario
 * (actualizar el estado del ejemplar).
 */
@Service
public class DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final PrestamoService prestamoService;
    private final InventarioService inventarioService;

    public DevolucionService(DevolucionRepository devolucionRepository, PrestamoService prestamoService,
                             InventarioService inventarioService) {
        this.devolucionRepository = devolucionRepository;
        this.prestamoService = prestamoService;
        this.inventarioService = inventarioService;
    }

    @Transactional
    public DevolucionResponse registrarDevolucion(DevolucionRequest solicitud) {
        String codigo = InventarioService.normalizarCodigo(solicitud.codigoEjemplar());

        // El ejemplar debe existir en el inventario.
        inventarioService.consultarDisponibilidad(codigo);

        // Regla 1: el préstamo debe estar pendiente de devolución.
        Prestamo prestamo = prestamoService.buscarPrestamoActivo(codigo);

        // Regla 2: se calculan los días de retraso respecto a la fecha de vencimiento.
        LocalDate hoy = LocalDate.now();
        long diasRetraso = Math.max(0, ChronoUnit.DAYS.between(prestamo.getFechaVencimiento(), hoy));
        prestamo.registrarDevolucion(hoy);

        // Regla 3: el ejemplar se actualiza según su condición: disponible o en reparación.
        EstadoEjemplar nuevoEstado = solicitud.condicion() == CondicionEjemplar.DANADO
                ? EstadoEjemplar.EN_REPARACION
                : EstadoEjemplar.DISPONIBLE;
        inventarioService.actualizarEstado(codigo, nuevoEstado);

        String observaciones = solicitud.observaciones() == null || solicitud.observaciones().isBlank()
                ? null : solicitud.observaciones().trim();
        Devolucion devolucion = devolucionRepository.save(
                new Devolucion(prestamo, hoy, solicitud.condicion(), diasRetraso, observaciones));
        return DevolucionResponse.desde(devolucion);
    }
}

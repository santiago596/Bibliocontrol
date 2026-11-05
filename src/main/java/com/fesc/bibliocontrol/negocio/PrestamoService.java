package com.fesc.bibliocontrol.negocio;

import com.fesc.bibliocontrol.dto.PrestamoRequest;
import com.fesc.bibliocontrol.dto.PrestamoResponse;
import com.fesc.bibliocontrol.negocio.politica.PoliticaPrestamo;
import com.fesc.bibliocontrol.persistencia.entidad.Ejemplar;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoEjemplar;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoPrestamo;
import com.fesc.bibliocontrol.persistencia.entidad.Prestamo;
import com.fesc.bibliocontrol.persistencia.entidad.TipoUsuario;
import com.fesc.bibliocontrol.persistencia.entidad.Usuario;
import com.fesc.bibliocontrol.persistencia.repositorio.PrestamoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del negocio del componente Gestión de Préstamos (caso testigo).
 * Depende de Gestión de Usuarios (identificar al usuario) y de Gestión de Inventario
 * (verificar disponibilidad y actualizar el estado del ejemplar).
 */
@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioService usuarioService;
    private final InventarioService inventarioService;
    private final List<PoliticaPrestamo> politicas;

    public PrestamoService(PrestamoRepository prestamoRepository, UsuarioService usuarioService,
                           InventarioService inventarioService, List<PoliticaPrestamo> politicas) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioService = usuarioService;
        this.inventarioService = inventarioService;
        this.politicas = politicas;
    }

    /**
     * Registra el préstamo de un ejemplar. Toda la operación es una transacción:
     * si alguna regla falla, no se guarda el préstamo ni cambia el estado del ejemplar.
     */
    @Transactional
    public PrestamoResponse registrarPrestamo(PrestamoRequest solicitud) {
        // Regla 1: el usuario debe estar registrado y activo.
        Usuario usuario = usuarioService.validarUsuarioActivo(solicitud.documentoUsuario());

        // Regla 2: el ejemplar debe estar disponible (se consulta a Gestión de Inventario).
        String codigo = InventarioService.normalizarCodigo(solicitud.codigoEjemplar());
        EstadoEjemplar estado = inventarioService.consultarDisponibilidad(codigo);
        if (estado != EstadoEjemplar.DISPONIBLE) {
            throw new ReglaNegocioException(
                    "El ejemplar " + codigo + " no está disponible (estado actual: " + estado + ").");
        }

        // Regla 3: el plazo se calcula con la política (Strategy) del tipo de usuario.
        PoliticaPrestamo politica = seleccionarPolitica(usuario.getTipo());
        LocalDate hoy = LocalDate.now();
        LocalDate vencimiento = hoy.plusDays(politica.calcularPlazo());

        // Regla 4: al registrar el préstamo el ejemplar pasa a PRESTADO.
        Ejemplar ejemplar = inventarioService.actualizarEstado(codigo, EstadoEjemplar.PRESTADO);
        Prestamo prestamo = prestamoRepository.save(new Prestamo(usuario, ejemplar, hoy, vencimiento));
        return PrestamoResponse.desde(prestamo);
    }

    /** Lista los préstamos pendientes de devolución. */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> consultarPrestamosActivos() {
        return prestamoRepository.findByEstadoOrderByIdDesc(EstadoPrestamo.ACTIVO).stream()
                .map(PrestamoResponse::desde)
                .toList();
    }

    /** Busca el préstamo activo de un ejemplar; lo usa Gestión de Devoluciones. */
    @Transactional(readOnly = true)
    public Prestamo buscarPrestamoActivo(String codigoEjemplar) {
        String codigo = InventarioService.normalizarCodigo(codigoEjemplar);
        return prestamoRepository.findByEjemplarCodigoAndEstado(codigo, EstadoPrestamo.ACTIVO)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El ejemplar " + codigo + " no tiene un préstamo pendiente de devolución."));
    }

    private PoliticaPrestamo seleccionarPolitica(TipoUsuario tipo) {
        return politicas.stream()
                .filter(p -> p.aplicaA(tipo))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "No hay una política de préstamo definida para " + tipo + "."));
    }
}

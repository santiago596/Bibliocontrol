package com.fesc.bibliocontrol.negocio;

import com.fesc.bibliocontrol.dto.PrestamoResponse;
import com.fesc.bibliocontrol.dto.ResumenResponse;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoEjemplar;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoPrestamo;
import com.fesc.bibliocontrol.persistencia.entidad.Prestamo;
import com.fesc.bibliocontrol.persistencia.repositorio.EjemplarRepository;
import com.fesc.bibliocontrol.persistencia.repositorio.LibroRepository;
import com.fesc.bibliocontrol.persistencia.repositorio.PrestamoRepository;
import com.fesc.bibliocontrol.persistencia.repositorio.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del negocio del componente Consultas e Historial:
 * historial de préstamos (general o por usuario) e indicadores del sistema.
 */
@Service
public class ConsultaService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public ConsultaService(PrestamoRepository prestamoRepository, LibroRepository libroRepository,
                           EjemplarRepository ejemplarRepository, UsuarioRepository usuarioRepository,
                           UsuarioService usuarioService) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    /**
     * Historial de préstamos. Se puede filtrar por identificación del usuario
     * y por estado: ACTIVO, DEVUELTO o VENCIDO.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> consultarHistorial(String documento, String estado) {
        Predicate<Prestamo> filtro = crearFiltro(estado);
        List<Prestamo> prestamos;
        if (documento == null || documento.isBlank()) {
            prestamos = prestamoRepository.findAllByOrderByIdDesc();
        } else {
            // El usuario debe existir para consultar su historial.
            usuarioService.buscarPorDocumento(documento);
            prestamos = prestamoRepository.findByUsuarioDocumentoOrderByIdDesc(documento.trim());
        }
        return prestamos.stream().filter(filtro).map(PrestamoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public ResumenResponse consultarResumen() {
        return new ResumenResponse(
                libroRepository.count(),
                ejemplarRepository.count(),
                ejemplarRepository.countByEstado(EstadoEjemplar.DISPONIBLE),
                ejemplarRepository.countByEstado(EstadoEjemplar.PRESTADO),
                ejemplarRepository.countByEstado(EstadoEjemplar.EN_REPARACION),
                prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO),
                prestamoRepository.countByEstadoAndFechaVencimientoBefore(EstadoPrestamo.ACTIVO, LocalDate.now()),
                usuarioRepository.countByActivoTrue());
    }

    private static Predicate<Prestamo> crearFiltro(String estado) {
        if (estado == null || estado.isBlank()) {
            return p -> true;
        }
        LocalDate hoy = LocalDate.now();
        return switch (estado.trim().toUpperCase()) {
            case "ACTIVO" -> Prestamo::estaActivo;
            case "DEVUELTO" -> p -> p.getEstado() == EstadoPrestamo.DEVUELTO;
            case "VENCIDO" -> p -> p.estaVencido(hoy);
            default -> throw new ReglaNegocioException(
                    "Filtro de estado no válido: " + estado + ". Use ACTIVO, DEVUELTO o VENCIDO.");
        };
    }
}

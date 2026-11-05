package com.fesc.bibliocontrol.negocio;

import com.fesc.bibliocontrol.dto.AgregarEjemplaresRequest;
import com.fesc.bibliocontrol.dto.EjemplarResponse;
import com.fesc.bibliocontrol.dto.LibroRequest;
import com.fesc.bibliocontrol.dto.LibroResponse;
import com.fesc.bibliocontrol.dto.ModificarLibroRequest;
import com.fesc.bibliocontrol.persistencia.entidad.Ejemplar;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoEjemplar;
import com.fesc.bibliocontrol.persistencia.entidad.Libro;
import com.fesc.bibliocontrol.persistencia.repositorio.EjemplarRepository;
import com.fesc.bibliocontrol.persistencia.repositorio.LibroRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del negocio del componente Gestión de Inventario:
 * registrar, modificar y consultar libros, registrar ejemplares,
 * consultar disponibilidad y actualizar el estado de los ejemplares.
 */
@Service
public class InventarioService {

    private static final int MAX_EJEMPLARES = 50;

    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;

    public InventarioService(LibroRepository libroRepository, EjemplarRepository ejemplarRepository) {
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
    }

    /** Registra un libro y crea sus ejemplares en estado DISPONIBLE. */
    @Transactional
    public LibroResponse registrarLibro(LibroRequest datos) {
        String isbn = normalizarIsbn(datos.isbn());

        // Regla: el ISBN debe tener 10 o 13 caracteres.
        if (isbn.length() != 10 && isbn.length() != 13) {
            throw new ReglaNegocioException(
                    "El ISBN debe tener 10 o 13 dígitos (se recibieron " + isbn.length() + ").");
        }
        // Regla: no puede existir otro libro con el mismo ISBN.
        if (libroRepository.existsByIsbn(isbn)) {
            throw new ReglaNegocioException("Ya existe un libro registrado con el ISBN " + isbn + ".");
        }
        validarCantidad(datos.cantidadEjemplares());

        Libro libro = new Libro(datos.titulo().trim(), datos.autor().trim(), datos.categoria().trim(), isbn);
        crearEjemplares(libro, datos.cantidadEjemplares(), datos.ubicacion());
        return LibroResponse.desde(libroRepository.save(libro));
    }

    /** Modifica título, autor y categoría de un libro. */
    @Transactional
    public LibroResponse modificarLibro(Long id, ModificarLibroRequest datos) {
        Libro libro = buscarLibro(id);
        libro.actualizar(datos.titulo().trim(), datos.autor().trim(), datos.categoria().trim());
        return LibroResponse.desde(libro);
    }

    /** Registra nuevas copias físicas de un libro existente. */
    @Transactional
    public LibroResponse agregarEjemplares(Long id, AgregarEjemplaresRequest datos) {
        Libro libro = buscarLibro(id);
        validarCantidad(datos.cantidad());
        crearEjemplares(libro, datos.cantidad(), datos.ubicacion());
        return LibroResponse.desde(libroRepository.saveAndFlush(libro));
    }

    /** Consulta todos los libros, o los que coinciden por título, autor o ISBN. */
    @Transactional(readOnly = true)
    public List<LibroResponse> consultarLibros(String criterio) {
        List<Libro> libros;
        if (criterio == null || criterio.isBlank()) {
            libros = libroRepository.findAllByOrderByTituloAsc();
        } else {
            String texto = criterio.trim();
            libros = libroRepository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrIsbnOrderByTituloAsc(
                    texto, texto, normalizarIsbn(texto));
        }
        return libros.stream().map(LibroResponse::desde).toList();
    }

    /** Devuelve el estado actual de un ejemplar. Lo usan Préstamos y Devoluciones. */
    @Transactional(readOnly = true)
    public EstadoEjemplar consultarDisponibilidad(String codigoEjemplar) {
        return buscarEjemplar(codigoEjemplar).getEstado();
    }

    /** Cambia el estado de un ejemplar como resultado de un préstamo o una devolución. */
    @Transactional
    public Ejemplar actualizarEstado(String codigoEjemplar, EstadoEjemplar nuevoEstado) {
        Ejemplar ejemplar = buscarEjemplar(codigoEjemplar);
        ejemplar.cambiarEstado(nuevoEstado);
        return ejemplar;
    }

    /** Cambio manual de estado por el bibliotecario (por ejemplo, enviar a reparación). */
    @Transactional
    public EjemplarResponse cambiarEstadoManual(String codigoEjemplar, EstadoEjemplar nuevoEstado) {
        Ejemplar ejemplar = buscarEjemplar(codigoEjemplar);
        // Regla: el estado PRESTADO solo lo asigna Gestión de Préstamos.
        if (nuevoEstado == EstadoEjemplar.PRESTADO) {
            throw new ReglaNegocioException("El estado PRESTADO solo se asigna al registrar un préstamo.");
        }
        // Regla: un ejemplar prestado solo cambia de estado al registrar su devolución.
        if (ejemplar.getEstado() == EstadoEjemplar.PRESTADO) {
            throw new ReglaNegocioException("El ejemplar " + ejemplar.getCodigo()
                    + " está prestado; su estado cambiará al registrar la devolución.");
        }
        ejemplar.cambiarEstado(nuevoEstado);
        return EjemplarResponse.desde(ejemplar);
    }

    private void crearEjemplares(Libro libro, int cantidad, String ubicacion) {
        String ubicacionLimpia = ubicacion == null || ubicacion.isBlank() ? null : ubicacion.trim();
        int siguiente = libro.getEjemplares().size() + 1;
        for (int i = 0; i < cantidad; i++) {
            libro.agregarEjemplar(libro.getIsbn() + "-" + (siguiente + i), ubicacionLimpia);
        }
    }

    private static void validarCantidad(Integer cantidad) {
        // Regla: se registra al menos un ejemplar (se exige aunque la solicitud no venga de la interfaz).
        if (cantidad == null || cantidad < 1 || cantidad > MAX_EJEMPLARES) {
            throw new ReglaNegocioException("La cantidad de ejemplares debe estar entre 1 y " + MAX_EJEMPLARES + ".");
        }
    }

    private Libro buscarLibro(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro con id " + id + "."));
    }

    private Ejemplar buscarEjemplar(String codigoEjemplar) {
        String codigo = normalizarCodigo(codigoEjemplar);
        return ejemplarRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un ejemplar con el código " + codigo + "."));
    }

    static String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private static String normalizarIsbn(String isbn) {
        return isbn.replace("-", "").replace(" ", "").toUpperCase();
    }
}

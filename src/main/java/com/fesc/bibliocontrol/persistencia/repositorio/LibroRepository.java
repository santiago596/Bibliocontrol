package com.fesc.bibliocontrol.persistencia.repositorio;

import com.fesc.bibliocontrol.persistencia.entidad.Libro;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de libros. Spring Data JPA genera la implementación (consultas SQL)
 * a partir del nombre de cada método.
 */
public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    @EntityGraph(attributePaths = "ejemplares")
    List<Libro> findAllByOrderByTituloAsc();

    @EntityGraph(attributePaths = "ejemplares")
    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrIsbnOrderByTituloAsc(
            String titulo, String autor, String isbn);
}

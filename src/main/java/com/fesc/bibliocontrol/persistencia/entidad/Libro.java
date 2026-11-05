package com.fesc.bibliocontrol.persistencia.entidad;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad JPA que representa la información general de un libro (tabla "libros").
 * Un libro tiene uno o varios ejemplares (copias físicas).
 */
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(nullable = false, length = 80)
    private String categoria;

    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ejemplar> ejemplares = new ArrayList<>();

    protected Libro() {
        // Requerido por JPA
    }

    public Libro(String titulo, String autor, String categoria, String isbn) {
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.isbn = isbn;
    }

    /** Modifica la información general del libro (el ISBN no cambia). */
    public void actualizar(String titulo, String autor, String categoria) {
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
    }

    /** Crea un ejemplar disponible asociado a este libro. */
    public Ejemplar agregarEjemplar(String codigo, String ubicacion) {
        Ejemplar ejemplar = new Ejemplar(codigo, ubicacion, this);
        ejemplares.add(ejemplar);
        return ejemplar;
    }

    public long contarDisponibles() {
        return ejemplares.stream().filter(Ejemplar::estaDisponible).count();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getIsbn() {
        return isbn;
    }

    public List<Ejemplar> getEjemplares() {
        return Collections.unmodifiableList(ejemplares);
    }
}

package com.fesc.bibliocontrol.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Entidad JPA que representa cada copia física de un libro y su estado (tabla "ejemplares").
 */
@Entity
@Table(name = "ejemplares")
public class Ejemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEjemplar estado = EstadoEjemplar.DISPONIBLE;

    @Column(length = 100)
    private String ubicacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "libro_id")
    private Libro libro;

    /** Control de concurrencia: evita que dos préstamos simultáneos tomen el mismo ejemplar. */
    @Version
    private Long version;

    protected Ejemplar() {
        // Requerido por JPA
    }

    Ejemplar(String codigo, String ubicacion, Libro libro) {
        this.codigo = codigo;
        this.ubicacion = ubicacion;
        this.libro = libro;
    }

    public boolean estaDisponible() {
        return estado == EstadoEjemplar.DISPONIBLE;
    }

    public void cambiarEstado(EstadoEjemplar nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public EstadoEjemplar getEstado() {
        return estado;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public Libro getLibro() {
        return libro;
    }
}

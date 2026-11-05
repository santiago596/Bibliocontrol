package com.fesc.bibliocontrol.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Entidad JPA que registra la devolución de un préstamo (tabla "devoluciones"). */
@Entity
@Table(name = "devoluciones")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "prestamo_id", unique = true)
    private Prestamo prestamo;

    @Column(nullable = false)
    private LocalDate fechaDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CondicionEjemplar condicion;

    @Column(nullable = false)
    private long diasRetraso;

    @Column(length = 255)
    private String observaciones;

    protected Devolucion() {
        // Requerido por JPA
    }

    public Devolucion(Prestamo prestamo, LocalDate fechaDevolucion, CondicionEjemplar condicion,
                      long diasRetraso, String observaciones) {
        this.prestamo = prestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.condicion = condicion;
        this.diasRetraso = diasRetraso;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public CondicionEjemplar getCondicion() {
        return condicion;
    }

    public long getDiasRetraso() {
        return diasRetraso;
    }

    public String getObservaciones() {
        return observaciones;
    }
}

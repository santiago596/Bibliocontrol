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
import java.time.LocalDate;

/** Entidad JPA que registra el préstamo de un ejemplar a un usuario (tabla "prestamos"). */
@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ejemplar_id")
    private Ejemplar ejemplar;

    @Column(nullable = false)
    private LocalDate fechaPrestamo;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    private LocalDate fechaDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    protected Prestamo() {
        // Requerido por JPA
    }

    public Prestamo(Usuario usuario, Ejemplar ejemplar, LocalDate fechaPrestamo, LocalDate fechaVencimiento) {
        this.usuario = usuario;
        this.ejemplar = ejemplar;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
    }

    /** Marca el préstamo como devuelto en la fecha indicada. */
    public void registrarDevolucion(LocalDate fecha) {
        this.fechaDevolucion = fecha;
        this.estado = EstadoPrestamo.DEVUELTO;
    }

    public boolean estaActivo() {
        return estado == EstadoPrestamo.ACTIVO;
    }

    /** Un préstamo está vencido si sigue activo y su fecha de vencimiento ya pasó. */
    public boolean estaVencido(LocalDate hoy) {
        return estaActivo() && fechaVencimiento.isBefore(hoy);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Ejemplar getEjemplar() {
        return ejemplar;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }
}

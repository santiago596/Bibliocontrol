package com.fesc.bibliocontrol.persistencia.repositorio;

import com.fesc.bibliocontrol.persistencia.entidad.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de devoluciones. */
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
}

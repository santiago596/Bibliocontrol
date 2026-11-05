package com.fesc.bibliocontrol.persistencia.repositorio;

import com.fesc.bibliocontrol.persistencia.entidad.Ejemplar;
import com.fesc.bibliocontrol.persistencia.entidad.EstadoEjemplar;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de ejemplares (copias físicas). */
public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {

    Optional<Ejemplar> findByCodigo(String codigo);

    long countByEstado(EstadoEjemplar estado);
}

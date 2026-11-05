package com.fesc.bibliocontrol.persistencia.repositorio;

import com.fesc.bibliocontrol.persistencia.entidad.EstadoPrestamo;
import com.fesc.bibliocontrol.persistencia.entidad.Prestamo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de préstamos: guarda y consulta los registros de préstamo. */
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByEstadoOrderByIdDesc(EstadoPrestamo estado);

    List<Prestamo> findAllByOrderByIdDesc();

    List<Prestamo> findByUsuarioDocumentoOrderByIdDesc(String documento);

    Optional<Prestamo> findByEjemplarCodigoAndEstado(String codigoEjemplar, EstadoPrestamo estado);

    long countByEstado(EstadoPrestamo estado);

    long countByEstadoAndFechaVencimientoBefore(EstadoPrestamo estado, LocalDate fecha);
}

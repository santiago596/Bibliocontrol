package com.fesc.bibliocontrol.persistencia.repositorio;

import com.fesc.bibliocontrol.persistencia.entidad.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio de usuarios de la biblioteca. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    List<Usuario> findAllByOrderByNombreAsc();

    List<Usuario> findByDocumentoContainingOrNombreContainingIgnoreCaseOrderByNombreAsc(
            String documento, String nombre);

    long countByActivoTrue();
}

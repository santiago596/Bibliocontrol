package com.fesc.bibliocontrol.negocio;

import com.fesc.bibliocontrol.dto.UsuarioRequest;
import com.fesc.bibliocontrol.dto.UsuarioResponse;
import com.fesc.bibliocontrol.persistencia.entidad.Usuario;
import com.fesc.bibliocontrol.persistencia.repositorio.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del negocio del componente Gestión de Usuarios:
 * registrar, modificar, consultar, activar y desactivar usuarios de la biblioteca.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UsuarioResponse registrarUsuario(UsuarioRequest datos) {
        String documento = datos.documento().trim();
        // Regla: la identificación no puede repetirse.
        if (usuarioRepository.existsByDocumento(documento)) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con la identificación " + documento + ".");
        }
        Usuario usuario = new Usuario(documento, datos.nombre().trim(), limpiar(datos.email()),
                limpiar(datos.telefono()), datos.tipo(), true);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse modificarUsuario(Long id, UsuarioRequest datos) {
        Usuario usuario = buscarPorId(id);
        String documento = datos.documento().trim();
        if (!documento.equals(usuario.getDocumento()) && usuarioRepository.existsByDocumento(documento)) {
            throw new ReglaNegocioException("Ya existe otro usuario con la identificación " + documento + ".");
        }
        usuario.actualizar(documento, datos.nombre().trim(), limpiar(datos.email()),
                limpiar(datos.telefono()), datos.tipo());
        return UsuarioResponse.desde(usuario);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = buscarPorId(id);
        usuario.cambiarEstado(activo);
        return UsuarioResponse.desde(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> consultarUsuarios(String criterio) {
        List<Usuario> usuarios = criterio == null || criterio.isBlank()
                ? usuarioRepository.findAllByOrderByNombreAsc()
                : usuarioRepository.findByDocumentoContainingOrNombreContainingIgnoreCaseOrderByNombreAsc(
                        criterio.trim(), criterio.trim());
        return usuarios.stream().map(UsuarioResponse::desde).toList();
    }

    /** Busca un usuario por su identificación; lo usan Préstamos y Consultas. */
    @Transactional(readOnly = true)
    public Usuario buscarPorDocumento(String documento) {
        return usuarioRepository.findByDocumento(documento.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un usuario registrado con la identificación " + documento.trim() + "."));
    }

    /** Regla del préstamo: el usuario debe estar registrado y activo. */
    @Transactional(readOnly = true)
    public Usuario validarUsuarioActivo(String documento) {
        Usuario usuario = buscarPorDocumento(documento);
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException(
                    "El usuario " + usuario.getNombre() + " está inactivo y no puede recibir préstamos.");
        }
        return usuario;
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id + "."));
    }

    private static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

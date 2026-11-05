package com.fesc.bibliocontrol.acceso;

import com.fesc.bibliocontrol.dto.CambioEstadoUsuarioRequest;
import com.fesc.bibliocontrol.dto.UsuarioRequest;
import com.fesc.bibliocontrol.dto.UsuarioResponse;
import com.fesc.bibliocontrol.negocio.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Acceso al negocio del componente Gestión de Usuarios.
 * Recibe las solicitudes de UsuarioView y las delega a UsuarioService.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** POST /api/usuarios: registra un usuario. */
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrarUsuario(@Valid @RequestBody UsuarioRequest solicitud) {
        UsuarioResponse usuario = usuarioService.registrarUsuario(solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.id())).body(usuario);
    }

    /** GET /api/usuarios?criterio=...: consulta usuarios por identificación o nombre. */
    @GetMapping
    public List<UsuarioResponse> consultarUsuarios(@RequestParam(required = false) String criterio) {
        return usuarioService.consultarUsuarios(criterio);
    }

    /** PUT /api/usuarios/{id}: modifica los datos de un usuario. */
    @PutMapping("/{id}")
    public UsuarioResponse modificarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequest solicitud) {
        return usuarioService.modificarUsuario(id, solicitud);
    }

    /** PATCH /api/usuarios/{id}/estado: activa o desactiva un usuario. */
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambioEstadoUsuarioRequest solicitud) {
        return usuarioService.cambiarEstado(id, solicitud.activo());
    }
}

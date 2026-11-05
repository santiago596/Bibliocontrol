package com.fesc.bibliocontrol.dto;

import com.fesc.bibliocontrol.persistencia.entidad.Usuario;

/** Información de un usuario devuelta a la presentación. */
public record UsuarioResponse(
        Long id,
        String documento,
        String nombre,
        String email,
        String telefono,
        String tipo,
        boolean activo) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getDocumento(), usuario.getNombre(),
                usuario.getEmail(), usuario.getTelefono(), usuario.getTipo().name(), usuario.isActivo());
    }
}

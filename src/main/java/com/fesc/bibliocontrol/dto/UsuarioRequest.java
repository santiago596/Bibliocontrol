package com.fesc.bibliocontrol.dto;

import com.fesc.bibliocontrol.persistencia.entidad.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos capturados por UsuarioView para registrar o modificar un usuario. */
public record UsuarioRequest(
        @NotBlank(message = "La identificación es obligatoria")
        @Pattern(regexp = "^\\s*[0-9]{5,15}\\s*$", message = "La identificación debe tener entre 5 y 15 dígitos")
        String documento,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @Email(message = "El correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String email,

        @Pattern(regexp = "^[0-9+ ]{0,20}$", message = "El teléfono solo puede contener números")
        String telefono,

        @NotNull(message = "El tipo de usuario es obligatorio")
        TipoUsuario tipo) {
}

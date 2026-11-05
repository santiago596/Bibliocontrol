// UsuarioView: captura los datos de los usuarios, envía las solicitudes a UsuarioController
// y muestra los resultados.

const formUsuario = document.getElementById('formUsuario');
const formBusqueda = document.getElementById('formBusqueda');
const mensajeUsuario = document.getElementById('mensajeUsuario');
const mensajeTabla = document.getElementById('mensajeTabla');
const tablaUsuarios = document.getElementById('tablaUsuarios');

const dialogo = document.getElementById('dialogoUsuario');
const formModificar = document.getElementById('formModificar');
const mensajeDialogo = document.getElementById('mensajeDialogo');

let usuariosCargados = [];
let usuarioSeleccionado = null;

formUsuario.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    ocultarMensaje(mensajeUsuario);
    if (!formUsuario.checkValidity()) {
        mostrarMensaje(mensajeUsuario, 'error',
            'Revise los datos: la identificación (5 a 15 dígitos) y el nombre son obligatorios, y el correo debe ser válido.');
        return;
    }
    const datos = Object.fromEntries(new FormData(formUsuario));
    try {
        const usuario = await enviarSolicitud('/api/usuarios', { method: 'POST', body: JSON.stringify(datos) });
        mostrarMensaje(mensajeUsuario, 'exito', `Usuario ${usuario.nombre} (${usuario.documento}) registrado.`);
        formUsuario.reset();
        await cargarUsuarios();
    } catch (error) {
        mostrarMensaje(mensajeUsuario, 'error', error.message, error.detalles);
    }
});

formBusqueda.addEventListener('submit', (evento) => {
    evento.preventDefault();
    cargarUsuarios();
});

async function cargarUsuarios() {
    const criterio = new FormData(formBusqueda).get('criterio');
    const url = criterio ? '/api/usuarios?criterio=' + encodeURIComponent(criterio) : '/api/usuarios';
    try {
        usuariosCargados = await enviarSolicitud(url);
        mostrarUsuarios();
    } catch (error) {
        tablaUsuarios.innerHTML = `<tr><td colspan="7">${escaparHtml(error.message)}</td></tr>`;
    }
}

function mostrarUsuarios() {
    if (usuariosCargados.length === 0) {
        tablaUsuarios.innerHTML = '<tr><td colspan="7" class="ayuda">No se encontraron usuarios.</td></tr>';
        return;
    }
    tablaUsuarios.innerHTML = usuariosCargados.map(u => `
        <tr>
            <td>${escaparHtml(u.documento)}</td>
            <td>${escaparHtml(u.nombre)}</td>
            <td>${escaparHtml(u.email || '—')}</td>
            <td>${escaparHtml(u.telefono || '—')}</td>
            <td>${escaparHtml(u.tipo)}</td>
            <td>${u.activo ? etiqueta('ACTIVO_USUARIO', 'Activo') : etiqueta('INACTIVO', 'Inactivo')}</td>
            <td class="acciones">
                <button class="pequeno secundario" data-accion="editar" data-id="${u.id}">Editar</button>
                <button class="pequeno secundario" data-accion="estado" data-id="${u.id}">
                    ${u.activo ? 'Desactivar' : 'Activar'}</button>
            </td>
        </tr>`).join('');
}

tablaUsuarios.addEventListener('click', async (evento) => {
    const boton = evento.target.closest('button[data-accion]');
    if (!boton) {
        return;
    }
    const usuario = usuariosCargados.find(u => u.id === Number(boton.dataset.id));
    if (boton.dataset.accion === 'editar') {
        abrirDialogo(usuario);
        return;
    }
    ocultarMensaje(mensajeTabla);
    try {
        const actualizado = await enviarSolicitud(`/api/usuarios/${usuario.id}/estado`,
            { method: 'PATCH', body: JSON.stringify({ activo: !usuario.activo }) });
        mostrarMensaje(mensajeTabla, 'exito',
            `${actualizado.nombre} ahora está ${actualizado.activo ? 'activo' : 'inactivo'}.`);
        await cargarUsuarios();
    } catch (error) {
        mostrarMensaje(mensajeTabla, 'error', error.message, error.detalles);
    }
});

function abrirDialogo(usuario) {
    usuarioSeleccionado = usuario;
    ocultarMensaje(mensajeDialogo);
    formModificar.documento.value = usuario.documento;
    formModificar.nombre.value = usuario.nombre;
    formModificar.email.value = usuario.email || '';
    formModificar.telefono.value = usuario.telefono || '';
    formModificar.tipo.value = usuario.tipo;
    dialogo.showModal();
}

formModificar.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    if (!formModificar.checkValidity()) {
        mostrarMensaje(mensajeDialogo, 'error', 'Revise los datos del formulario.');
        return;
    }
    const datos = Object.fromEntries(new FormData(formModificar));
    try {
        await enviarSolicitud(`/api/usuarios/${usuarioSeleccionado.id}`, { method: 'PUT', body: JSON.stringify(datos) });
        mostrarMensaje(mensajeDialogo, 'exito', 'Datos del usuario actualizados.');
        await cargarUsuarios();
    } catch (error) {
        mostrarMensaje(mensajeDialogo, 'error', error.message, error.detalles);
    }
});

document.getElementById('cerrarDialogo').addEventListener('click', () => dialogo.close());

cargarUsuarios();

// Utilidades compartidas por todas las vistas: menú, envío de solicitudes y mensajes.

const PAGINAS = [
    ['index.html', 'Inicio'],
    ['libros.html', 'Inventario'],
    ['usuarios.html', 'Usuarios'],
    ['prestamos.html', 'Préstamos'],
    ['devoluciones.html', 'Devoluciones'],
    ['historial.html', 'Historial']
];

/** Dibuja el encabezado con el menú de navegación y marca la página actual. */
(function dibujarEncabezado() {
    const encabezado = document.getElementById('encabezado');
    if (!encabezado) {
        return;
    }
    const actual = location.pathname.split('/').pop() || 'index.html';
    encabezado.innerHTML = `
        <h1>Bibliocontrol · Biblioteca universitaria</h1>
        <nav>${PAGINAS.map(([url, texto]) =>
            `<a href="${url}" class="${url === actual ? 'activo' : ''}">${texto}</a>`).join('')}</nav>`;
})();

/** Envía una solicitud HTTP al componente de acceso (controlador) y devuelve el JSON de respuesta. */
async function enviarSolicitud(url, opciones = {}) {
    const respuesta = await fetch(url, {
        headers: { 'Content-Type': 'application/json' },
        ...opciones
    });
    const cuerpo = await respuesta.json().catch(() => null);
    if (!respuesta.ok) {
        const error = new Error(cuerpo?.mensaje || 'Ocurrió un error inesperado en el servidor.');
        error.detalles = cuerpo?.detalles || [];
        throw error;
    }
    return cuerpo;
}

/** Muestra un mensaje de éxito o de error en el elemento indicado. */
function mostrarMensaje(elemento, tipo, texto, detalles = []) {
    elemento.className = 'mensaje ' + tipo;
    const lista = detalles.length
        ? '<ul>' + detalles.map(d => '<li>' + escaparHtml(d) + '</li>').join('') + '</ul>'
        : '';
    elemento.innerHTML = escaparHtml(texto) + lista;
}

function ocultarMensaje(elemento) {
    elemento.className = 'mensaje';
    elemento.innerHTML = '';
}

/** Etiqueta de color para un estado (DISPONIBLE, PRESTADO, ACTIVO, VENCIDO...). */
function etiqueta(estado, texto = estado) {
    return `<span class="estado ${escaparHtml(estado)}">${escaparHtml(texto)}</span>`;
}

/** Convierte una fecha ISO (2026-11-04) a formato local (04/11/2026). */
function formatearFecha(fechaIso) {
    if (!fechaIso) {
        return '—';
    }
    const [anio, mes, dia] = fechaIso.split('-');
    return `${dia}/${mes}/${anio}`;
}

function escaparHtml(valor) {
    return String(valor ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;');
}

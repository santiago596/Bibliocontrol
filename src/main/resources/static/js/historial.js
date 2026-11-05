// HistorialView: envía los filtros a ConsultaController (/api/historial) y muestra los préstamos.

const formFiltro = document.getElementById('formFiltro');
const mensajeHistorial = document.getElementById('mensajeHistorial');
const tablaHistorial = document.getElementById('tablaHistorial');

formFiltro.addEventListener('submit', (evento) => {
    evento.preventDefault();
    cargarHistorial();
});

async function cargarHistorial() {
    ocultarMensaje(mensajeHistorial);
    const filtros = new URLSearchParams();
    const { documento, estado } = Object.fromEntries(new FormData(formFiltro));
    if (documento.trim()) {
        filtros.set('documento', documento.trim());
    }
    if (estado) {
        filtros.set('estado', estado);
    }
    try {
        const prestamos = await enviarSolicitud('/api/historial?' + filtros);
        tablaHistorial.innerHTML = prestamos.length === 0
            ? '<tr><td colspan="8" class="ayuda">No hay préstamos con esos filtros.</td></tr>'
            : prestamos.map(p => `
                <tr>
                    <td>${p.id}</td>
                    <td>${escaparHtml(p.nombreUsuario)}<br><span class="ayuda">${escaparHtml(p.documentoUsuario)}</span></td>
                    <td>${escaparHtml(p.codigoEjemplar)}</td>
                    <td>${escaparHtml(p.tituloLibro)}</td>
                    <td>${formatearFecha(p.fechaPrestamo)}</td>
                    <td>${formatearFecha(p.fechaVencimiento)}</td>
                    <td>${formatearFecha(p.fechaDevolucion)}</td>
                    <td>${p.vencido ? etiqueta('VENCIDO', 'Vencido') : etiqueta(p.estado, p.estado === 'ACTIVO' ? 'Activo' : 'Devuelto')}</td>
                </tr>`).join('');
    } catch (error) {
        tablaHistorial.innerHTML = '';
        mostrarMensaje(mensajeHistorial, 'error', error.message, error.detalles);
    }
}

cargarHistorial();

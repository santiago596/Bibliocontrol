// DevolucionView: captura el ejemplar devuelto y su condición, envía la solicitud a
// DevolucionController y muestra el resultado.

const formDevolucion = document.getElementById('formDevolucion');
const mensajeDevolucion = document.getElementById('mensajeDevolucion');
const tablaPendientes = document.getElementById('tablaPendientes');

formDevolucion.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    ocultarMensaje(mensajeDevolucion);
    if (!formDevolucion.checkValidity()) {
        mostrarMensaje(mensajeDevolucion, 'error', 'Ingrese el código del ejemplar.');
        return;
    }
    const datos = Object.fromEntries(new FormData(formDevolucion));
    const boton = formDevolucion.querySelector('button');
    boton.disabled = true;
    try {
        const d = await enviarSolicitud('/api/devoluciones', { method: 'POST', body: JSON.stringify(datos) });
        const retraso = d.diasRetraso > 0 ? ` Se entregó con ${d.diasRetraso} día(s) de retraso.` : ' Entregado a tiempo.';
        mostrarMensaje(mensajeDevolucion, 'exito',
            `Devolución registrada: "${d.tituloLibro}" (${d.codigoEjemplar}) de ${d.nombreUsuario}.`
            + `${retraso} Estado del ejemplar: ${d.estadoEjemplar}.`);
        formDevolucion.reset();
        await cargarPendientes();
    } catch (error) {
        mostrarMensaje(mensajeDevolucion, 'error', error.message, error.detalles);
    } finally {
        boton.disabled = false;
    }
});

async function cargarPendientes() {
    try {
        const prestamos = await enviarSolicitud('/api/prestamos');
        tablaPendientes.innerHTML = prestamos.length === 0
            ? '<tr><td colspan="6" class="ayuda">No hay préstamos pendientes.</td></tr>'
            : prestamos.map(p => `
                <tr>
                    <td>${escaparHtml(p.codigoEjemplar)}</td>
                    <td>${escaparHtml(p.tituloLibro)}</td>
                    <td>${escaparHtml(p.nombreUsuario)}</td>
                    <td>${formatearFecha(p.fechaVencimiento)}</td>
                    <td>${p.vencido ? etiqueta('VENCIDO', 'Vencido') : etiqueta('ACTIVO', 'Al día')}</td>
                    <td><button class="pequeno secundario" data-codigo="${escaparHtml(p.codigoEjemplar)}">Devolver</button></td>
                </tr>`).join('');
    } catch (error) {
        tablaPendientes.innerHTML = `<tr><td colspan="6">${escaparHtml(error.message)}</td></tr>`;
    }
}

// Al pulsar "Devolver" se completa el código en el formulario.
tablaPendientes.addEventListener('click', (evento) => {
    const boton = evento.target.closest('button[data-codigo]');
    if (boton) {
        formDevolucion.codigoEjemplar.value = boton.dataset.codigo;
        formDevolucion.condicion.focus();
    }
});

cargarPendientes();

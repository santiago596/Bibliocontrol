// PrestamoView: captura usuario y ejemplar, envía la solicitud a PrestamoController y muestra el resultado.

const formPrestamo = document.getElementById('formPrestamo');
const mensajePrestamo = document.getElementById('mensajePrestamo');
const tablaPrestamos = document.getElementById('tablaPrestamos');

formPrestamo.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    ocultarMensaje(mensajePrestamo);

    if (!formPrestamo.checkValidity()) {
        mostrarMensaje(mensajePrestamo, 'error', 'Ingrese la identificación del usuario y el código del ejemplar.');
        return;
    }

    const datos = Object.fromEntries(new FormData(formPrestamo));
    const boton = formPrestamo.querySelector('button');
    boton.disabled = true;
    try {
        // solicitarPrestamo(datos) -> PrestamoController.registrarPrestamo()
        const prestamo = await enviarSolicitud('/api/prestamos', { method: 'POST', body: JSON.stringify(datos) });
        mostrarMensaje(mensajePrestamo, 'exito',
            `Préstamo #${prestamo.id} registrado: "${prestamo.tituloLibro}" (${prestamo.codigoEjemplar}) `
            + `para ${prestamo.nombreUsuario}. Plazo de ${prestamo.plazoDias} días, vence el `
            + `${formatearFecha(prestamo.fechaVencimiento)}.`);
        formPrestamo.reset();
        await cargarPrestamos();
    } catch (error) {
        // Solicitud rechazada: se muestra el motivo devuelto por la lógica del negocio.
        mostrarMensaje(mensajePrestamo, 'error', error.message, error.detalles);
    } finally {
        boton.disabled = false;
    }
});

async function cargarPrestamos() {
    try {
        const prestamos = await enviarSolicitud('/api/prestamos');
        tablaPrestamos.innerHTML = prestamos.length === 0
            ? '<tr><td colspan="8" class="ayuda">No hay préstamos activos.</td></tr>'
            : prestamos.map(p => `
                <tr>
                    <td>${p.id}</td>
                    <td>${escaparHtml(p.nombreUsuario)}<br><span class="ayuda">${escaparHtml(p.documentoUsuario)}</span></td>
                    <td>${escaparHtml(p.tipoUsuario)}</td>
                    <td>${escaparHtml(p.codigoEjemplar)}</td>
                    <td>${escaparHtml(p.tituloLibro)}</td>
                    <td>${formatearFecha(p.fechaPrestamo)}</td>
                    <td>${formatearFecha(p.fechaVencimiento)}</td>
                    <td>${p.vencido ? etiqueta('VENCIDO', 'Vencido') : etiqueta('ACTIVO', 'Al día')}</td>
                </tr>`).join('');
    } catch (error) {
        tablaPrestamos.innerHTML = `<tr><td colspan="8">${escaparHtml(error.message)}</td></tr>`;
    }
}

cargarPrestamos();

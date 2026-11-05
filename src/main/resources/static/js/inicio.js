// Vista de inicio: muestra los indicadores que entrega ConsultaController (/api/resumen).

async function cargarResumen() {
    const contenedor = document.getElementById('indicadores');
    try {
        const r = await enviarSolicitud('/api/resumen');
        const indicadores = [
            ['Libros registrados', r.libros],
            ['Ejemplares', r.ejemplares],
            ['Ejemplares disponibles', r.ejemplaresDisponibles],
            ['Ejemplares prestados', r.ejemplaresPrestados],
            ['En reparación', r.ejemplaresEnReparacion],
            ['Préstamos activos', r.prestamosActivos],
            ['Préstamos vencidos', r.prestamosVencidos, r.prestamosVencidos > 0],
            ['Usuarios activos', r.usuariosActivos]
        ];
        contenedor.innerHTML = indicadores.map(([nombre, valor, alerta]) => `
            <div class="indicador ${alerta ? 'alerta' : ''}">
                <div class="valor">${valor}</div>
                <div class="nombre">${nombre}</div>
            </div>`).join('');
    } catch (error) {
        mostrarMensaje(document.getElementById('mensajeResumen'), 'error', error.message);
    }
}

cargarResumen();

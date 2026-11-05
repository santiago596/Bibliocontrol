// LibroView: captura los datos de libros y ejemplares, envía las solicitudes a
// LibroController / EjemplarController y muestra los resultados.

const formLibro = document.getElementById('formLibro');
const formBusqueda = document.getElementById('formBusqueda');
const mensajeLibro = document.getElementById('mensajeLibro');
const tablaLibros = document.getElementById('tablaLibros');

const dialogo = document.getElementById('dialogoLibro');
const formModificar = document.getElementById('formModificar');
const formEjemplares = document.getElementById('formEjemplares');
const tablaEjemplares = document.getElementById('tablaEjemplares');
const mensajeDialogo = document.getElementById('mensajeDialogo');

let librosCargados = [];
let libroSeleccionado = null;

// ---------- Registrar libro ----------
formLibro.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    ocultarMensaje(mensajeLibro);

    // Validación de interfaz (ayuda al usuario); las reglas reales se validan de nuevo en el servidor.
    if (!formLibro.checkValidity()) {
        mostrarMensaje(mensajeLibro, 'error', 'Complete los campos obligatorios: título, autor, categoría, ISBN y cantidad.');
        return;
    }
    const datos = Object.fromEntries(new FormData(formLibro));
    datos.cantidadEjemplares = Number(datos.cantidadEjemplares);

    await conBotonOcupado(formLibro, async () => {
        try {
            const libro = await enviarSolicitud('/api/libros', { method: 'POST', body: JSON.stringify(datos) });
            mostrarMensaje(mensajeLibro, 'exito',
                `Libro "${libro.titulo}" registrado con ${libro.totalEjemplares} ejemplar(es): `
                + libro.ejemplares.map(e => e.codigo).join(', '));
            formLibro.reset();
            await cargarLibros();
        } catch (error) {
            mostrarMensaje(mensajeLibro, 'error', error.message, error.detalles);
        }
    });
});

// ---------- Consultar libros ----------
formBusqueda.addEventListener('submit', (evento) => {
    evento.preventDefault();
    cargarLibros();
});

async function cargarLibros() {
    const criterio = new FormData(formBusqueda).get('criterio');
    const url = criterio ? '/api/libros?criterio=' + encodeURIComponent(criterio) : '/api/libros';
    try {
        librosCargados = await enviarSolicitud(url);
        mostrarLibros();
    } catch (error) {
        tablaLibros.innerHTML = `<tr><td colspan="7">${escaparHtml(error.message)}</td></tr>`;
    }
}

function mostrarLibros() {
    if (librosCargados.length === 0) {
        tablaLibros.innerHTML = '<tr><td colspan="7" class="ayuda">No se encontraron libros.</td></tr>';
        return;
    }
    tablaLibros.innerHTML = librosCargados.map(libro => `
        <tr>
            <td>${escaparHtml(libro.titulo)}</td>
            <td>${escaparHtml(libro.autor)}</td>
            <td>${escaparHtml(libro.categoria)}</td>
            <td>${escaparHtml(libro.isbn)}</td>
            <td>${libro.disponibles} de ${libro.totalEjemplares}</td>
            <td>${libro.ejemplares.map(e => etiqueta(e.estado, e.codigo)).join('')}</td>
            <td><button class="pequeno secundario" data-id="${libro.id}">Gestionar</button></td>
        </tr>`).join('');
}

tablaLibros.addEventListener('click', (evento) => {
    const boton = evento.target.closest('button[data-id]');
    if (boton) {
        abrirDialogo(Number(boton.dataset.id));
    }
});

// ---------- Gestionar libro (modificar, agregar ejemplares, cambiar estado) ----------
function abrirDialogo(id) {
    libroSeleccionado = librosCargados.find(l => l.id === id);
    ocultarMensaje(mensajeDialogo);
    formModificar.titulo.value = libroSeleccionado.titulo;
    formModificar.autor.value = libroSeleccionado.autor;
    formModificar.categoria.value = libroSeleccionado.categoria;
    formEjemplares.reset();
    mostrarDialogo();
    dialogo.showModal();
}

function mostrarDialogo() {
    document.getElementById('tituloDialogo').textContent =
        `${libroSeleccionado.titulo} · ISBN ${libroSeleccionado.isbn}`;
    tablaEjemplares.innerHTML = libroSeleccionado.ejemplares.map(e => `
        <tr>
            <td>${escaparHtml(e.codigo)}</td>
            <td>${escaparHtml(e.ubicacion || '—')}</td>
            <td>${etiqueta(e.estado)}</td>
            <td>${e.estado === 'PRESTADO'
                ? '<span class="ayuda">Cambia al registrar la devolución</span>'
                : `<button class="pequeno secundario" data-codigo="${escaparHtml(e.codigo)}"
                       data-estado="${e.estado === 'DISPONIBLE' ? 'EN_REPARACION' : 'DISPONIBLE'}">
                       ${e.estado === 'DISPONIBLE' ? 'Enviar a reparación' : 'Marcar disponible'}</button>`}
            </td>
        </tr>`).join('');
}

formModificar.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    if (!formModificar.checkValidity()) {
        mostrarMensaje(mensajeDialogo, 'error', 'Título, autor y categoría son obligatorios.');
        return;
    }
    const datos = Object.fromEntries(new FormData(formModificar));
    await guardarEnDialogo(`/api/libros/${libroSeleccionado.id}`, 'PUT', datos, 'Información del libro actualizada.');
});

formEjemplares.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    if (!formEjemplares.checkValidity()) {
        mostrarMensaje(mensajeDialogo, 'error', 'Indique una cantidad entre 1 y 50.');
        return;
    }
    const datos = Object.fromEntries(new FormData(formEjemplares));
    datos.cantidad = Number(datos.cantidad);
    await guardarEnDialogo(`/api/libros/${libroSeleccionado.id}/ejemplares`, 'POST', datos, 'Ejemplares agregados.');
    formEjemplares.reset();
});

tablaEjemplares.addEventListener('click', async (evento) => {
    const boton = evento.target.closest('button[data-codigo]');
    if (!boton) {
        return;
    }
    try {
        await enviarSolicitud(`/api/ejemplares/${encodeURIComponent(boton.dataset.codigo)}/estado`,
            { method: 'PATCH', body: JSON.stringify({ estado: boton.dataset.estado }) });
        await refrescarLibroSeleccionado();
        mostrarMensaje(mensajeDialogo, 'exito', `Estado del ejemplar ${boton.dataset.codigo} actualizado.`);
    } catch (error) {
        mostrarMensaje(mensajeDialogo, 'error', error.message, error.detalles);
    }
});

async function guardarEnDialogo(url, metodo, datos, textoExito) {
    try {
        libroSeleccionado = await enviarSolicitud(url, { method: metodo, body: JSON.stringify(datos) });
        mostrarDialogo();
        mostrarMensaje(mensajeDialogo, 'exito', textoExito);
        await cargarLibros();
    } catch (error) {
        mostrarMensaje(mensajeDialogo, 'error', error.message, error.detalles);
    }
}

async function refrescarLibroSeleccionado() {
    await cargarLibros();
    libroSeleccionado = librosCargados.find(l => l.id === libroSeleccionado.id) || libroSeleccionado;
    mostrarDialogo();
}

document.getElementById('cerrarDialogo').addEventListener('click', () => dialogo.close());

async function conBotonOcupado(formulario, accion) {
    const boton = formulario.querySelector('button[type="submit"]');
    boton.disabled = true;
    try {
        await accion();
    } finally {
        boton.disabled = false;
    }
}

cargarLibros();

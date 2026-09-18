
let listaClientes = [];
let idClienteAEliminar = null;
let temporizadorBusqueda = null;

document.addEventListener("DOMContentLoaded", () => {
  inicializarNavbar();

  configurarFormularioCliente();
  configurarModalEliminar();
  configurarBusqueda();
  cargarClientes();
});

function configurarBusqueda() {
  document.getElementById("campoBusqueda").addEventListener("input", (evento) => {
    clearTimeout(temporizadorBusqueda);
    const texto = evento.target.value;
    temporizadorBusqueda = setTimeout(() => cargarClientes(texto), 300);
  });
}

async function cargarClientes(busqueda) {
  const cuerpoTabla = document.getElementById("cuerpoTablaClientes");
  try {
    const ruta = busqueda ? `/clientes?buscar=${encodeURIComponent(busqueda)}` : "/clientes";
    listaClientes = await solicitarApi(ruta);
    renderizarTabla();
  } catch (error) {
    cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">${escaparHtml(error.message)}</td></tr>`;
  }
}

function renderizarTabla() {
  const cuerpoTabla = document.getElementById("cuerpoTablaClientes");
  const contador = document.getElementById("contadorResultados");

  contador.textContent = `${listaClientes.length} cliente(s) encontrado(s)`;

  if (listaClientes.length === 0) {
    cuerpoTabla.innerHTML = `
      <tr>
        <td colspan="6">
          <div class="pila-vacia">
            <i class="bi bi-inbox d-block mb-2"></i>
            No se encontraron clientes.
          </div>
        </td>
      </tr>
    `;
    return;
  }

  cuerpoTabla.innerHTML = listaClientes
    .map(
      (cliente) => `
        <tr>
          <td class="codigo-producto">${escaparHtml(cliente.documento)}</td>
          <td class="fw-semibold">${escaparHtml(cliente.nombre)}</td>
          <td>${cliente.telefono ? escaparHtml(cliente.telefono) : '<span class="text-muted">-</span>'}</td>
          <td>${cliente.correo ? escaparHtml(cliente.correo) : '<span class="text-muted">-</span>'}</td>
          <td>${formatearFecha(cliente.fechaRegistro)}</td>
          <td class="text-end">
            <button type="button" class="btn btn-outline-dark boton-accion-fila me-1" title="Historial de compras" onclick="abrirHistorial(${cliente.id})">
              <i class="bi bi-receipt"></i>
            </button>
            <button type="button" class="btn btn-outline-dark boton-accion-fila me-1" title="Editar" onclick="abrirModalEdicion(${cliente.id})">
              <i class="bi bi-pencil"></i>
            </button>
            <button type="button" class="btn btn-outline-danger boton-accion-fila" title="Eliminar" onclick="abrirModalEliminar(${cliente.id})">
              <i class="bi bi-trash"></i>
            </button>
          </td>
        </tr>
      `
    )
    .join("");
}



function configurarFormularioCliente() {
  const modalElemento = document.getElementById("modalCliente");
  const formulario = document.getElementById("formularioCliente");

  document.getElementById("botonNuevoCliente").addEventListener("click", () => {
    document.getElementById("tituloModalCliente").innerHTML = '<i class="bi bi-person-vcard"></i> Nuevo cliente';
    document.getElementById("clienteId").value = "";
  });

  modalElemento.addEventListener("hidden.bs.modal", () => {
    formulario.reset();
    limpiarAlerta(document.getElementById("contenedorAlertaModal"));
    ["errorNombreCliente", "errorDocumentoCliente", "errorCorreoCliente"].forEach((id) => (document.getElementById(id).textContent = ""));
  });

  formulario.addEventListener("submit", manejarEnvioFormulario);
}

function abrirModalEdicion(id) {
  const cliente = listaClientes.find((item) => item.id === id);
  if (!cliente) return;

  document.getElementById("tituloModalCliente").innerHTML = '<i class="bi bi-pencil-square"></i> Editar cliente';
  document.getElementById("clienteId").value = cliente.id;
  document.getElementById("nombreCliente").value = cliente.nombre;
  document.getElementById("documentoCliente").value = cliente.documento;
  document.getElementById("telefonoCliente").value = cliente.telefono || "";
  document.getElementById("correoCliente").value = cliente.correo || "";
  document.getElementById("direccionCliente").value = cliente.direccion || "";

  limpiarAlerta(document.getElementById("contenedorAlertaModal"));
  new bootstrap.Modal(document.getElementById("modalCliente")).show();
}

async function manejarEnvioFormulario(evento) {
  evento.preventDefault();
  ["errorNombreCliente", "errorDocumentoCliente", "errorCorreoCliente"].forEach((id) => (document.getElementById(id).textContent = ""));
  limpiarAlerta(document.getElementById("contenedorAlertaModal"));

  const id = document.getElementById("clienteId").value;
  const datos = {
    nombre: document.getElementById("nombreCliente").value.trim(),
    documento: document.getElementById("documentoCliente").value.trim(),
    telefono: document.getElementById("telefonoCliente").value.trim() || null,
    correo: document.getElementById("correoCliente").value.trim() || null,
    direccion: document.getElementById("direccionCliente").value.trim() || null,
  };

  let esValido = true;
  if (!datos.nombre) {
    document.getElementById("errorNombreCliente").textContent = "El nombre es obligatorio.";
    esValido = false;
  }
  if (!datos.documento) {
    document.getElementById("errorDocumentoCliente").textContent = "El documento es obligatorio.";
    esValido = false;
  }
  if (datos.correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(datos.correo)) {
    document.getElementById("errorCorreoCliente").textContent = "Ingresa un correo válido.";
    esValido = false;
  }
  if (!esValido) return;

  const boton = document.getElementById("botonGuardarCliente");
  const spinner = document.getElementById("spinnerGuardarCliente");
  const texto = document.getElementById("textoBotonGuardarCliente");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Guardando...";

  try {
    if (id) {
      await solicitarApi(`/clientes/${id}`, { method: "PUT", body: JSON.stringify(datos) });
    } else {
      await solicitarApi("/clientes", { method: "POST", body: JSON.stringify(datos) });
    }

    bootstrap.Modal.getInstance(document.getElementById("modalCliente")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), `Cliente <strong>${escaparHtml(datos.nombre)}</strong> guardado correctamente.`, "success");
    await cargarClientes();
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlertaModal"), error.message || "No se pudo guardar el cliente.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Guardar cliente";
  }
}



async function abrirHistorial(id) {
  const cliente = listaClientes.find((item) => item.id === id);
  if (!cliente) return;

  document.getElementById("nombreClienteHistorial").textContent = cliente.nombre;
  const contenedor = document.getElementById("cuerpoHistorialCompras");
  contenedor.innerHTML = `<div class="text-center text-muted py-4"><div class="spinner-border spinner-border-sm me-2"></div> Cargando historial...</div>`;

  new bootstrap.Modal(document.getElementById("modalHistorial")).show();

  try {
    const ventas = await solicitarApi(`/clientes/${id}/compras`);
    renderizarHistorial(ventas, contenedor);
  } catch (error) {
    contenedor.innerHTML = `<div class="alert alert-danger">${escaparHtml(error.message)}</div>`;
  }
}

function renderizarHistorial(ventas, contenedor) {
  if (!ventas || ventas.length === 0) {
    contenedor.innerHTML = `
      <div class="pila-vacia">
        <i class="bi bi-inbox d-block mb-2"></i>
        Este cliente aún no tiene compras registradas.
      </div>
    `;
    return;
  }

  const claseBadge = { PENDIENTE: "badge-pendiente", COMPLETADA: "badge-completada", ANULADA: "badge-anulada" };

  contenedor.innerHTML = `
    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead>
          <tr>
            <th>#Venta</th>
            <th>Fecha</th>
            <th class="text-center">Estado</th>
            <th class="text-end">Total</th>
            <th class="text-end">Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${ventas
            .map(
              (venta) => `
              <tr>
                <td class="fw-semibold">#${venta.id}</td>
                <td>${formatearFechaHora(venta.fechaCreacion)}</td>
                <td class="text-center"><span class="badge-estado ${claseBadge[venta.estado]}">${traducirEstadoVenta(venta.estado)}</span></td>
                <td class="text-end">${formatearMoneda(venta.total)}</td>
                <td class="text-end">
                  <a class="btn btn-outline-dark boton-accion-fila" title="Ver venta" href="ventas.html?ventaId=${venta.id}">
                    <i class="bi bi-eye"></i>
                  </a>
                </td>
              </tr>
            `
            )
            .join("")}
        </tbody>
      </table>
    </div>
  `;
}


function configurarModalEliminar() {
  document.getElementById("botonConfirmarEliminar").addEventListener("click", confirmarEliminacion);
}

function abrirModalEliminar(id) {
  const cliente = listaClientes.find((item) => item.id === id);
  if (!cliente) return;

  idClienteAEliminar = id;
  document.getElementById("nombreClienteEliminar").textContent = cliente.nombre;
  new bootstrap.Modal(document.getElementById("modalConfirmarEliminar")).show();
}

async function confirmarEliminacion() {
  if (!idClienteAEliminar) return;

  const boton = document.getElementById("botonConfirmarEliminar");
  const spinner = document.getElementById("spinnerEliminar");
  const texto = document.getElementById("textoBotonEliminar");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Eliminando...";

  try {
    await solicitarApi(`/clientes/${idClienteAEliminar}`, { method: "DELETE" });
    bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEliminar")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), "Cliente eliminado correctamente.", "success");
    idClienteAEliminar = null;
    await cargarClientes();
  } catch (error) {
    bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEliminar")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message || "No se pudo eliminar el cliente.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Sí, eliminar";
  }
}

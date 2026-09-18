let listaProveedores = [];
let idProveedorAEliminar = null;
let usuarioActual = null;

document.addEventListener("DOMContentLoaded", () => {
  usuarioActual = inicializarNavbar();

  if (esUsuarioAdmin(usuarioActual)) {
    document.getElementById("botonNuevoProveedor").classList.remove("d-none");
  }

  configurarFormularioProveedor();
  configurarModalEliminar();
  cargarProveedores();
});

async function cargarProveedores() {
  const cuerpoTabla = document.getElementById("cuerpoTablaProveedores");
  try {
    listaProveedores = await solicitarApi("/proveedores");
    renderizarTabla();
  } catch (error) {
    cuerpoTabla.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">${escaparHtml(error.message)}</td></tr>`;
  }
}

function renderizarTabla() {
  const cuerpoTabla = document.getElementById("cuerpoTablaProveedores");
  const contador = document.getElementById("contadorResultados");
  const puedeGestionar = esUsuarioAdmin(usuarioActual);

  contador.textContent = `${listaProveedores.length} proveedor(es) encontrado(s)`;

  if (listaProveedores.length === 0) {
    cuerpoTabla.innerHTML = `
      <tr>
        <td colspan="5">
          <div class="pila-vacia">
            <i class="bi bi-inbox d-block mb-2"></i>
            No hay proveedores registrados.
          </div>
        </td>
      </tr>
    `;
    return;
  }

  cuerpoTabla.innerHTML = listaProveedores
    .map(
      (proveedor) => `
        <tr>
          <td class="fw-semibold">${escaparHtml(proveedor.nombre)}</td>
          <td>${proveedor.nombreContacto ? escaparHtml(proveedor.nombreContacto) : '<span class="text-muted">-</span>'}</td>
          <td>${proveedor.telefono ? escaparHtml(proveedor.telefono) : '<span class="text-muted">-</span>'}</td>
          <td>${proveedor.correo ? escaparHtml(proveedor.correo) : '<span class="text-muted">-</span>'}</td>
          <td class="text-end">
            ${
              puedeGestionar
                ? `
              <button type="button" class="btn btn-outline-dark boton-accion-fila me-1" title="Editar" onclick="abrirModalEdicion(${proveedor.id})">
                <i class="bi bi-pencil"></i>
              </button>
              <button type="button" class="btn btn-outline-danger boton-accion-fila" title="Eliminar" onclick="abrirModalEliminar(${proveedor.id})">
                <i class="bi bi-trash"></i>
              </button>`
                : '<span class="text-muted small">Solo lectura</span>'
            }
          </td>
        </tr>
      `
    )
    .join("");
}


function configurarFormularioProveedor() {
  const modalElemento = document.getElementById("modalProveedor");
  const formulario = document.getElementById("formularioProveedor");

  document.getElementById("botonNuevoProveedor").addEventListener("click", () => {
    document.getElementById("tituloModalProveedor").innerHTML = '<i class="bi bi-truck"></i> Nuevo proveedor';
    document.getElementById("proveedorId").value = "";
  });

  modalElemento.addEventListener("hidden.bs.modal", () => {
    formulario.reset();
    limpiarAlerta(document.getElementById("contenedorAlertaModal"));
    ["errorNombreProveedor", "errorCorreoProveedor"].forEach((id) => (document.getElementById(id).textContent = ""));
  });

  formulario.addEventListener("submit", manejarEnvioFormulario);
}

function abrirModalEdicion(id) {
  const proveedor = listaProveedores.find((item) => item.id === id);
  if (!proveedor) return;

  document.getElementById("tituloModalProveedor").innerHTML = '<i class="bi bi-pencil-square"></i> Editar proveedor';
  document.getElementById("proveedorId").value = proveedor.id;
  document.getElementById("nombreProveedor").value = proveedor.nombre;
  document.getElementById("contactoProveedor").value = proveedor.nombreContacto || "";
  document.getElementById("telefonoProveedor").value = proveedor.telefono || "";
  document.getElementById("correoProveedor").value = proveedor.correo || "";

  limpiarAlerta(document.getElementById("contenedorAlertaModal"));
  new bootstrap.Modal(document.getElementById("modalProveedor")).show();
}

async function manejarEnvioFormulario(evento) {
  evento.preventDefault();
  ["errorNombreProveedor", "errorCorreoProveedor"].forEach((id) => (document.getElementById(id).textContent = ""));
  limpiarAlerta(document.getElementById("contenedorAlertaModal"));

  const id = document.getElementById("proveedorId").value;
  const datos = {
    nombre: document.getElementById("nombreProveedor").value.trim(),
    nombreContacto: document.getElementById("contactoProveedor").value.trim() || null,
    telefono: document.getElementById("telefonoProveedor").value.trim() || null,
    correo: document.getElementById("correoProveedor").value.trim() || null,
  };

  let esValido = true;
  if (!datos.nombre) {
    document.getElementById("errorNombreProveedor").textContent = "El nombre es obligatorio.";
    esValido = false;
  }
  if (datos.correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(datos.correo)) {
    document.getElementById("errorCorreoProveedor").textContent = "Ingresa un correo válido.";
    esValido = false;
  }
  if (!esValido) return;

  const boton = document.getElementById("botonGuardarProveedor");
  const spinner = document.getElementById("spinnerGuardarProveedor");
  const texto = document.getElementById("textoBotonGuardarProveedor");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Guardando...";

  try {
    if (id) {
      await solicitarApi(`/proveedores/${id}`, { method: "PUT", body: JSON.stringify(datos) });
    } else {
      await solicitarApi("/proveedores", { method: "POST", body: JSON.stringify(datos) });
    }

    bootstrap.Modal.getInstance(document.getElementById("modalProveedor")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), `Proveedor <strong>${escaparHtml(datos.nombre)}</strong> guardado correctamente.`, "success");
    await cargarProveedores();
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlertaModal"), error.message || "No se pudo guardar el proveedor.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Guardar proveedor";
  }
}


function configurarModalEliminar() {
  document.getElementById("botonConfirmarEliminar").addEventListener("click", confirmarEliminacion);
}

function abrirModalEliminar(id) {
  const proveedor = listaProveedores.find((item) => item.id === id);
  if (!proveedor) return;

  idProveedorAEliminar = id;
  document.getElementById("nombreProveedorEliminar").textContent = proveedor.nombre;
  new bootstrap.Modal(document.getElementById("modalConfirmarEliminar")).show();
}

async function confirmarEliminacion() {
  if (!idProveedorAEliminar) return;

  const boton = document.getElementById("botonConfirmarEliminar");
  const spinner = document.getElementById("spinnerEliminar");
  const texto = document.getElementById("textoBotonEliminar");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Eliminando...";

  try {
    await solicitarApi(`/proveedores/${idProveedorAEliminar}`, { method: "DELETE" });
    bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEliminar")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), "Proveedor eliminado correctamente.", "success");
    idProveedorAEliminar = null;
    await cargarProveedores();
  } catch (error) {
    bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEliminar")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message || "No se pudo eliminar el proveedor.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Sí, eliminar";
  }
}

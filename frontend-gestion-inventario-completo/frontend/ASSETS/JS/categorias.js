
let listaCategorias = [];
let idCategoriaAEliminar = null;
let usuarioActual = null;

document.addEventListener("DOMContentLoaded", () => {
  usuarioActual = inicializarNavbar();

  configurarPermisosPagina();
  configurarFormularioCategoria();
  configurarModalEliminar();
  cargarCategorias();
});

function esAdministrador() {
  return esUsuarioAdmin(usuarioActual);
}

function configurarPermisosPagina() {
  if (esAdministrador()) {
    document.getElementById("botonNuevaCategoria").classList.remove("d-none");
    document.getElementById("contenedorEstadoCategoria").classList.remove("d-none");
  }
}

async function cargarCategorias() {
  const cuerpoTabla = document.getElementById("cuerpoTablaCategorias");
  try {
    listaCategorias = await solicitarApi("/categorias");
    renderizarTabla();
  } catch (error) {
    cuerpoTabla.innerHTML = `<tr><td colspan="4" class="text-center text-danger py-4">${escaparHtml(error.message)}</td></tr>`;
  }
}

function renderizarTabla() {
  const cuerpoTabla = document.getElementById("cuerpoTablaCategorias");
  const contador = document.getElementById("contadorResultados");
  const puedeGestionar = esAdministrador();

  contador.textContent = `${listaCategorias.length} categoría(s) encontrada(s)`;

  if (listaCategorias.length === 0) {
    cuerpoTabla.innerHTML = `
      <tr>
        <td colspan="4">
          <div class="pila-vacia">
            <i class="bi bi-inbox d-block mb-2"></i>
            No hay categorías registradas.
          </div>
        </td>
      </tr>
    `;
    return;
  }

  cuerpoTabla.innerHTML = listaCategorias
    .map((categoria) => {
      const claseBadge = { PENDIENTE: "badge-pendiente", ACTIVA: "badge-activa", ELIMINADA: "badge-eliminada" }[categoria.estado];
      return `
        <tr>
          <td class="fw-semibold">${escaparHtml(categoria.nombre)}</td>
          <td>${categoria.descripcion ? escaparHtml(categoria.descripcion) : '<span class="text-muted">-</span>'}</td>
          <td class="text-center"><span class="badge-estado ${claseBadge}">${traducirEstadoCategoria(categoria.estado)}</span></td>
          <td class="text-end">
            ${
              puedeGestionar
                ? `
              <button type="button" class="btn btn-outline-dark boton-accion-fila me-1" title="Editar" onclick="abrirModalEdicion(${categoria.id})">
                <i class="bi bi-pencil"></i>
              </button>
              <button type="button" class="btn btn-outline-danger boton-accion-fila" title="Eliminar" onclick="abrirModalEliminar(${categoria.id})">
                <i class="bi bi-trash"></i>
              </button>`
                : '<span class="text-muted small">Solo lectura</span>'
            }
          </td>
        </tr>
      `;
    })
    .join("");
}



function configurarFormularioCategoria() {
  const modalElemento = document.getElementById("modalCategoria");
  const formulario = document.getElementById("formularioCategoria");

  document.getElementById("botonNuevaCategoria").addEventListener("click", prepararFormularioParaCrear);

  modalElemento.addEventListener("hidden.bs.modal", () => {
    formulario.reset();
    limpiarAlerta(document.getElementById("contenedorAlertaModal"));
    document.getElementById("errorNombreCategoria").textContent = "";
  });

  formulario.addEventListener("submit", manejarEnvioFormulario);
}

function prepararFormularioParaCrear() {
  document.getElementById("tituloModalCategoria").innerHTML = '<i class="bi bi-tags"></i> Nueva categoría';
  document.getElementById("categoriaId").value = "";
  document.getElementById("contenedorEstadoCategoria").classList.add("d-none");
}

function abrirModalEdicion(id) {
  const categoria = listaCategorias.find((item) => item.id === id);
  if (!categoria) return;

  document.getElementById("tituloModalCategoria").innerHTML = '<i class="bi bi-pencil-square"></i> Editar categoría';
  document.getElementById("categoriaId").value = categoria.id;
  document.getElementById("nombreCategoria").value = categoria.nombre;
  document.getElementById("descripcionCategoria").value = categoria.descripcion || "";
  document.getElementById("estadoCategoria").value = categoria.estado === "ELIMINADA" ? "ACTIVA" : categoria.estado;

  if (esAdministrador()) {
    document.getElementById("contenedorEstadoCategoria").classList.remove("d-none");
  }

  limpiarAlerta(document.getElementById("contenedorAlertaModal"));
  new bootstrap.Modal(document.getElementById("modalCategoria")).show();
}

async function manejarEnvioFormulario(evento) {
  evento.preventDefault();
  document.getElementById("errorNombreCategoria").textContent = "";
  limpiarAlerta(document.getElementById("contenedorAlertaModal"));

  const id = document.getElementById("categoriaId").value;
  const nombre = document.getElementById("nombreCategoria").value.trim();
  const descripcion = document.getElementById("descripcionCategoria").value.trim() || null;

  if (!nombre) {
    document.getElementById("errorNombreCategoria").textContent = "El nombre es obligatorio.";
    return;
  }

  const boton = document.getElementById("botonGuardarCategoria");
  const spinner = document.getElementById("spinnerGuardarCategoria");
  const texto = document.getElementById("textoBotonGuardarCategoria");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Guardando...";

  try {
    if (id) {
      const datos = { nombre, descripcion, estado: document.getElementById("estadoCategoria").value || null };
      await solicitarApi(`/categorias/${id}`, { method: "PUT", body: JSON.stringify(datos) });
    } else {
      await solicitarApi("/categorias", { method: "POST", body: JSON.stringify({ nombre, descripcion }) });
    }

    bootstrap.Modal.getInstance(document.getElementById("modalCategoria")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), `Categoría <strong>${escaparHtml(nombre)}</strong> guardada correctamente.`, "success");
    await cargarCategorias();
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlertaModal"), error.message || "No se pudo guardar la categoría.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Guardar categoría";
  }
}



function configurarModalEliminar() {
  document.getElementById("botonConfirmarEliminar").addEventListener("click", confirmarEliminacion);
}

function abrirModalEliminar(id) {
  const categoria = listaCategorias.find((item) => item.id === id);
  if (!categoria) return;

  idCategoriaAEliminar = id;
  document.getElementById("nombreCategoriaEliminar").textContent = categoria.nombre;
  new bootstrap.Modal(document.getElementById("modalConfirmarEliminar")).show();
}

async function confirmarEliminacion() {
  if (!idCategoriaAEliminar) return;

  const boton = document.getElementById("botonConfirmarEliminar");
  const spinner = document.getElementById("spinnerEliminar");
  const texto = document.getElementById("textoBotonEliminar");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Eliminando...";

  try {
    await solicitarApi(`/categorias/${idCategoriaAEliminar}`, { method: "DELETE" });
    bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEliminar")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), "Categoría eliminada correctamente.", "success");
    idCategoriaAEliminar = null;
    await cargarCategorias();
  } catch (error) {
    bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEliminar")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message || "No se pudo eliminar la categoría.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Sí, eliminar";
  }
}

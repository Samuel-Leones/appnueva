
let listaCategorias = [];
let listaProveedores = [];
let listaProductos = [];
let idProductoAEliminar = null;

const ROLES_QUE_PUEDEN_ELIMINAR = ["ADMINISTRADOR", "SUPER_USUARIO"];

document.addEventListener("DOMContentLoaded", () => {
  inicializarNavbar();

  configurarFormularioProducto();
  configurarModalEliminar();
  configurarFiltros();

  cargarDatosIniciales();
});



function puedeEliminarProductos() {
  const usuario = obtenerUsuarioActual();
  return usuario && ROLES_QUE_PUEDEN_ELIMINAR.includes(usuario.rol);
}



async function cargarDatosIniciales() {
  await Promise.all([cargarCategorias(), cargarProveedores()]);
  await cargarProductos();
  cargarEstadisticasAdministrador();
}

async function cargarCategorias() {
  try {
    listaCategorias = await solicitarApi("/categorias");
    poblarSelectCategorias();
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message, "danger");
  }
}

async function cargarProveedores() {
  try {
    listaProveedores = await solicitarApi("/proveedores");
    poblarSelectProveedores();
  } catch (error) {
    // Los proveedores son secundarios para el formulario; si falla, se deja vacío.
    console.warn("No se pudieron cargar los proveedores:", error.message);
  }
}

async function cargarProductos() {
  const cuerpoTabla = document.getElementById("cuerpoTablaProductos");
  try {
    listaProductos = await solicitarApi("/productos");
    aplicarFiltrosYRenderizar();
    actualizarTarjetasResumen();
  } catch (error) {
    cuerpoTabla.innerHTML = `<tr><td colspan="8" class="text-center text-danger py-4">${escaparHtml(error.message)}</td></tr>`;
  }
}


async function cargarEstadisticasAdministrador() {
  try {
    const estadisticas = await solicitarApi("/panel/estadisticas");
    document.getElementById("valorVentasMes").textContent = formatearMoneda(estadisticas.totalVentasDelMes);
    document.getElementById("valorClientes").textContent = estadisticas.cantidadClientesRegistrados;
    document.getElementById("tarjetaVentasMes").classList.remove("d-none");
    document.getElementById("tarjetaClientes").classList.remove("d-none");
  } catch (error) {
    // Rol sin permiso (EMPLEADO) u otro error: simplemente no se muestran estas tarjetas.
  }
}



function poblarSelectCategorias() {
  const filtro = document.getElementById("filtroCategoria");
  const formulario = document.getElementById("categoriaProducto");

  const categoriasActivas = listaCategorias.filter((categoria) => categoria.estado === "ACTIVA");

  filtro.innerHTML = '<option value="">Todas las categorías</option>' +
    listaCategorias.map((categoria) => `<option value="${categoria.id}">${escaparHtml(categoria.nombre)}</option>`).join("");

  formulario.innerHTML = '<option value="" selected disabled>Selecciona una categoría</option>' +
    categoriasActivas.map((categoria) => `<option value="${categoria.id}">${escaparHtml(categoria.nombre)}</option>`).join("");

  document.getElementById("valorTotalCategorias").textContent = categoriasActivas.length;
}

function poblarSelectProveedores() {
  const select = document.getElementById("proveedorProducto");
  select.innerHTML = '<option value="">Sin proveedor</option>' +
    listaProveedores.map((proveedor) => `<option value="${proveedor.id}">${escaparHtml(proveedor.nombre)}</option>`).join("");
}



function actualizarTarjetasResumen() {
  const total = listaProductos.length;
  const agotados = listaProductos.filter((producto) => producto.stockActual === 0).length;
  const stockBajo = listaProductos.filter((producto) => producto.stockBajo && producto.stockActual > 0).length;

  document.getElementById("valorTotalProductos").textContent = total;
  document.getElementById("valorAgotados").textContent = agotados;
  document.getElementById("valorStockBajo").textContent = stockBajo;
}



function configurarFiltros() {
  document.getElementById("campoBusqueda").addEventListener("input", aplicarFiltrosYRenderizar);
  document.getElementById("filtroCategoria").addEventListener("change", aplicarFiltrosYRenderizar);
  document.getElementById("filtroEstado").addEventListener("change", aplicarFiltrosYRenderizar);
  document.getElementById("botonLimpiarFiltros").addEventListener("click", () => {
    document.getElementById("campoBusqueda").value = "";
    document.getElementById("filtroCategoria").value = "";
    document.getElementById("filtroEstado").value = "";
    aplicarFiltrosYRenderizar();
  });
}

function aplicarFiltrosYRenderizar() {
  const texto = document.getElementById("campoBusqueda").value.trim().toLowerCase();
  const categoriaId = document.getElementById("filtroCategoria").value;
  const estado = document.getElementById("filtroEstado").value;

  const productosFiltrados = listaProductos.filter((producto) => {
    const coincideTexto =
      !texto ||
      producto.nombre.toLowerCase().includes(texto) ||
      producto.codigo.toLowerCase().includes(texto);

    const coincideCategoria = !categoriaId || String(producto.categoriaId) === categoriaId;

    const coincideEstado = !estado || estadoStock(producto) === estado;

    return coincideTexto && coincideCategoria && coincideEstado;
  });

  renderizarTabla(productosFiltrados);
}

function estadoStock(producto) {
  if (producto.stockActual === 0) return "AGOTADO";
  if (producto.stockBajo) return "STOCK_BAJO";
  return "DISPONIBLE";
}

function renderizarTabla(productos) {
  const cuerpoTabla = document.getElementById("cuerpoTablaProductos");
  const contador = document.getElementById("contadorResultados");
  const puedeEliminar = puedeEliminarProductos();

  contador.textContent = `${productos.length} producto(s) encontrado(s)`;

  if (productos.length === 0) {
    cuerpoTabla.innerHTML = `
      <tr>
        <td colspan="8">
          <div class="pila-vacia">
            <i class="bi bi-inbox d-block mb-2"></i>
            No se encontraron productos con los filtros actuales.
          </div>
        </td>
      </tr>
    `;
    return;
  }

  cuerpoTabla.innerHTML = productos
    .map((producto) => {
      const estado = estadoStock(producto);
      const claseBadge = {
        DISPONIBLE: "badge-disponible",
        STOCK_BAJO: "badge-stock-bajo",
        AGOTADO: "badge-agotado",
      }[estado];
      const textoBadge = {
        DISPONIBLE: "Disponible",
        STOCK_BAJO: "Stock bajo",
        AGOTADO: "Agotado",
      }[estado];

      return `
        <tr>
          <td><span class="codigo-producto">${escaparHtml(producto.codigo)}</span></td>
          <td>
            <div class="nombre-producto">${escaparHtml(producto.nombre)}</div>
            ${producto.descripcion ? `<div class="descripcion-producto" title="${escaparHtml(producto.descripcion)}">${escaparHtml(producto.descripcion)}</div>` : ""}
          </td>
          <td>${escaparHtml(producto.nombreCategoria)}</td>
          <td>${producto.nombreProveedor ? escaparHtml(producto.nombreProveedor) : '<span class="text-muted">-</span>'}</td>
          <td class="text-end">${formatearMoneda(producto.precio)}</td>
          <td class="text-center">${producto.stockActual} <span class="text-muted small">/ mín. ${producto.stockMinimo}</span></td>
          <td class="text-center"><span class="badge-estado ${claseBadge}">${textoBadge}</span></td>
          <td class="text-end">
            <button type="button" class="btn btn-outline-dark boton-accion-fila me-1" title="Editar" onclick="abrirModalEdicion(${producto.id})">
              <i class="bi bi-pencil"></i>
            </button>
            ${
              puedeEliminar
                ? `<button type="button" class="btn btn-outline-danger boton-accion-fila" title="Eliminar" onclick="abrirModalEliminar(${producto.id})">
                    <i class="bi bi-trash"></i>
                  </button>`
                : ""
            }
          </td>
        </tr>
      `;
    })
    .join("");
}



function configurarFormularioProducto() {
  const modalElemento = document.getElementById("modalProducto");
  const formulario = document.getElementById("formularioProducto");

  document.getElementById("botonNuevoProducto").addEventListener("click", () => {
    prepararFormularioParaCrear();
  });

  modalElemento.addEventListener("hidden.bs.modal", () => {
    formulario.reset();
    limpiarAlerta(document.getElementById("contenedorAlertaModal"));
    limpiarErroresFormularioProducto();
  });

  formulario.addEventListener("submit", manejarEnvioFormularioProducto);
}

function prepararFormularioParaCrear() {
  document.getElementById("tituloModalProducto").innerHTML = '<i class="bi bi-box-seam"></i> Nuevo producto';
  document.getElementById("productoId").value = "";
  document.getElementById("etiquetaStock").textContent = "Stock inicial";
  document.getElementById("stockInicialProducto").disabled = false;
  document.getElementById("stockInicialProducto").value = 0;
  document.getElementById("ayudaStockActual").textContent = "";
  document.getElementById("codigoProducto").disabled = false;
}

function abrirModalEdicion(id) {
  const producto = listaProductos.find((item) => item.id === id);
  if (!producto) return;

  document.getElementById("tituloModalProducto").innerHTML = '<i class="bi bi-pencil-square"></i> Editar producto';
  document.getElementById("productoId").value = producto.id;
  document.getElementById("nombreProducto").value = producto.nombre;
  document.getElementById("codigoProducto").value = producto.codigo;
  document.getElementById("descripcionProducto").value = producto.descripcion || "";
  document.getElementById("categoriaProducto").value = producto.categoriaId;
  document.getElementById("proveedorProducto").value = producto.proveedorId || "";
  document.getElementById("precioProducto").value = producto.precio;
  document.getElementById("stockMinimoProducto").value = producto.stockMinimo;

  document.getElementById("etiquetaStock").textContent = "Stock actual";
  document.getElementById("stockInicialProducto").value = producto.stockActual;
  document.getElementById("stockInicialProducto").disabled = true;
  document.getElementById("ayudaStockActual").textContent = "El stock se administra desde movimientos de inventario, no desde este formulario.";

 
  const select = document.getElementById("categoriaProducto");
  if (![...select.options].some((opcion) => opcion.value === String(producto.categoriaId))) {
    const opcion = document.createElement("option");
    opcion.value = producto.categoriaId;
    opcion.textContent = producto.nombreCategoria;
    select.appendChild(opcion);
    select.value = producto.categoriaId;
  }

  limpiarAlerta(document.getElementById("contenedorAlertaModal"));
  new bootstrap.Modal(document.getElementById("modalProducto")).show();
}

async function manejarEnvioFormularioProducto(evento) {
  evento.preventDefault();
  limpiarErroresFormularioProducto();
  limpiarAlerta(document.getElementById("contenedorAlertaModal"));

  const id = document.getElementById("productoId").value;
  const datos = {
    nombre: document.getElementById("nombreProducto").value.trim(),
    descripcion: document.getElementById("descripcionProducto").value.trim() || null,
    codigo: document.getElementById("codigoProducto").value.trim(),
    categoriaId: Number(document.getElementById("categoriaProducto").value) || null,
    proveedorId: document.getElementById("proveedorProducto").value ? Number(document.getElementById("proveedorProducto").value) : null,
    precio: Number(document.getElementById("precioProducto").value),
    stockInicial: Number(document.getElementById("stockInicialProducto").value) || 0,
    stockMinimo: Number(document.getElementById("stockMinimoProducto").value) || 0,
  };

  if (!validarFormularioProducto(datos)) return;

  const boton = document.getElementById("botonGuardarProducto");
  const spinner = document.getElementById("spinnerGuardarProducto");
  const texto = document.getElementById("textoBotonGuardarProducto");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Guardando...";

  try {
    if (id) {
      await solicitarApi(`/productos/${id}`, { method: "PUT", body: JSON.stringify(datos) });
    } else {
      await solicitarApi("/productos", { method: "POST", body: JSON.stringify(datos) });
    }

    bootstrap.Modal.getInstance(document.getElementById("modalProducto")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), `Producto <strong>${escaparHtml(datos.nombre)}</strong> guardado correctamente.`, "success");
    await cargarProductos();
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlertaModal"), error.message || "No se pudo guardar el producto.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Guardar producto";
  }
}

function validarFormularioProducto(datos) {
  let esValido = true;

  if (!datos.nombre) {
    document.getElementById("errorNombreProducto").textContent = "El nombre es obligatorio.";
    esValido = false;
  }
  if (!datos.codigo) {
    document.getElementById("errorCodigoProducto").textContent = "El código es obligatorio.";
    esValido = false;
  }
  if (!datos.categoriaId) {
    document.getElementById("errorCategoriaProducto").textContent = "Selecciona una categoría.";
    esValido = false;
  }
  if (!datos.precio || datos.precio <= 0) {
    document.getElementById("errorPrecioProducto").textContent = "El precio debe ser mayor a 0.";
    esValido = false;
  }

  return esValido;
}

function limpiarErroresFormularioProducto() {
  ["errorNombreProducto", "errorCodigoProducto", "errorCategoriaProducto", "errorPrecioProducto"].forEach((idCampo) => {
    document.getElementById(idCampo).textContent = "";
  });
}



function configurarModalEliminar() {
  document.getElementById("botonConfirmarEliminar").addEventListener("click", confirmarEliminacion);
}

function abrirModalEliminar(id) {
  const producto = listaProductos.find((item) => item.id === id);
  if (!producto) return;

  idProductoAEliminar = id;
  document.getElementById("nombreProductoEliminar").textContent = producto.nombre;
  new bootstrap.Modal(document.getElementById("modalConfirmarEliminar")).show();
}

async function confirmarEliminacion() {
  if (!idProductoAEliminar) return;

  const boton = document.getElementById("botonConfirmarEliminar");
  const spinner = document.getElementById("spinnerEliminar");
  const texto = document.getElementById("textoBotonEliminar");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Eliminando...";

  try {
    await solicitarApi(`/productos/${idProductoAEliminar}`, { method: "DELETE" });
    bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEliminar")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), "Producto eliminado correctamente.", "success");
    idProductoAEliminar = null;
    await cargarProductos();
  } catch (error) {
    bootstrap.Modal.getInstance(document.getElementById("modalConfirmarEliminar")).hide();
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message || "No se pudo eliminar el producto.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Sí, eliminar";
  }
}


let listaClientesVenta = [];
let listaProductosVenta = [];
let carrito = []; 
let ventaActual = null;

const TASA_IVA_ESTIMADA = 0.19;

document.addEventListener("DOMContentLoaded", () => {
  inicializarNavbar();

  configurarFormularioNuevaVenta();
  configurarBusquedaVenta();
  cargarDatosIniciales();

  const parametros = new URLSearchParams(window.location.search);
  const ventaId = parametros.get("ventaId");
  if (ventaId) {
    document.getElementById("campoBuscarVenta").value = ventaId;
    buscarVentaPorId(Number(ventaId));
  }
});

async function cargarDatosIniciales() {
  try {
    const [clientes, productos] = await Promise.all([solicitarApi("/clientes"), solicitarApi("/productos")]);
    listaClientesVenta = clientes;
    listaProductosVenta = productos;
    poblarSelectClientes();
    poblarSelectProductos();
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message, "danger");
  }
}

function poblarSelectClientes() {
  const select = document.getElementById("clienteVenta");
  select.innerHTML =
    '<option value="" selected disabled>Selecciona un cliente</option>' +
    listaClientesVenta.map((cliente) => `<option value="${cliente.id}">${escaparHtml(cliente.nombre)} · ${escaparHtml(cliente.documento)}</option>`).join("");
}

function poblarSelectProductos() {
  const select = document.getElementById("productoVenta");
  select.innerHTML =
    '<option value="" selected disabled>Selecciona un producto</option>' +
    listaProductosVenta
      .map(
        (producto) =>
          `<option value="${producto.id}">${escaparHtml(producto.codigo)} · ${escaparHtml(producto.nombre)} (stock: ${producto.stockActual})</option>`
      )
      .join("");
}



function configurarFormularioNuevaVenta() {
  document.getElementById("botonAgregarItem").addEventListener("click", agregarItemAlCarrito);
  document.getElementById("botonCrearVenta").addEventListener("click", crearVenta);
}

function agregarItemAlCarrito() {
  const productoId = Number(document.getElementById("productoVenta").value) || null;
  const cantidad = Number(document.getElementById("cantidadVenta").value);

  if (!productoId) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), "Selecciona un producto para agregarlo a la venta.", "warning");
    return;
  }
  if (!cantidad || cantidad <= 0) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), "La cantidad debe ser mayor a 0.", "warning");
    return;
  }

  const producto = listaProductosVenta.find((item) => item.id === productoId);
  if (!producto) return;

  const itemExistente = carrito.find((item) => item.productoId === productoId);
  if (itemExistente) {
    itemExistente.cantidad += cantidad;
  } else {
    carrito.push({
      productoId: producto.id,
      nombre: producto.nombre,
      codigo: producto.codigo,
      precio: Number(producto.precio),
      cantidad,
    });
  }

  document.getElementById("cantidadVenta").value = 1;
  document.getElementById("productoVenta").value = "";
  renderizarCarrito();
}

function quitarItemDelCarrito(productoId) {
  carrito = carrito.filter((item) => item.productoId !== productoId);
  renderizarCarrito();
}

function renderizarCarrito() {
  const cuerpo = document.getElementById("cuerpoCarrito");
  const botonCrear = document.getElementById("botonCrearVenta");

  if (carrito.length === 0) {
    cuerpo.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-3">Aún no has agregado productos a esta venta.</td></tr>`;
  } else {
    cuerpo.innerHTML = carrito
      .map(
        (item) => `
          <tr class="fila-carrito">
            <td>
              <div class="fw-semibold">${escaparHtml(item.nombre)}</div>
              <div class="text-muted small codigo-producto">${escaparHtml(item.codigo)}</div>
            </td>
            <td class="text-center">${item.cantidad}</td>
            <td class="text-end">${formatearMoneda(item.precio)}</td>
            <td class="text-end">${formatearMoneda(item.precio * item.cantidad)}</td>
            <td class="text-end">
              <button type="button" class="btn btn-sm btn-outline-danger" onclick="quitarItemDelCarrito(${item.productoId})">
                <i class="bi bi-x-lg"></i>
              </button>
            </td>
          </tr>
        `
      )
      .join("");
  }

  const subtotal = carrito.reduce((acumulado, item) => acumulado + item.precio * item.cantidad, 0);
  const iva = subtotal * TASA_IVA_ESTIMADA;
  const total = subtotal + iva;

  document.getElementById("valorSubtotalEstimado").textContent = formatearMoneda(subtotal);
  document.getElementById("valorIvaEstimado").textContent = formatearMoneda(iva);
  document.getElementById("valorTotalEstimado").textContent = formatearMoneda(total);

  botonCrear.disabled = carrito.length === 0;
}


async function crearVenta() {
  const clienteId = Number(document.getElementById("clienteVenta").value) || null;

  if (!clienteId) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), "Selecciona un cliente antes de crear la venta.", "warning");
    return;
  }
  if (carrito.length === 0) return;

  const boton = document.getElementById("botonCrearVenta");
  const spinner = document.getElementById("spinnerCrearVenta");
  const texto = document.getElementById("textoBotonCrearVenta");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Creando...";

  try {
    const datos = {
      clienteId,
      items: carrito.map((item) => ({ productoId: item.productoId, cantidad: item.cantidad })),
    };

    const venta = await solicitarApi("/ventas", { method: "POST", body: JSON.stringify(datos) });

    mostrarAlerta(document.getElementById("contenedorAlerta"), `Venta <strong>#${venta.id}</strong> creada como pendiente. Complétala para descontar el stock y generar la factura.`, "success");

    carrito = [];
    renderizarCarrito();
    document.getElementById("clienteVenta").value = "";

    ventaActual = venta;
    renderizarDetalleVenta(venta);
    document.getElementById("campoBuscarVenta").value = venta.id;

    await cargarDatosIniciales(); // refresca stock mostrado en selects
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message || "No se pudo crear la venta.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Crear venta";
  }
}


function configurarBusquedaVenta() {
  document.getElementById("botonBuscarVenta").addEventListener("click", () => {
    const id = Number(document.getElementById("campoBuscarVenta").value);
    if (id) buscarVentaPorId(id);
  });
}

async function buscarVentaPorId(id) {
  const contenedor = document.getElementById("contenedorDetalleVenta");
  contenedor.innerHTML = `<div class="text-center text-muted py-4"><div class="spinner-border spinner-border-sm me-2"></div> Buscando venta...</div>`;

  try {
    const venta = await solicitarApi(`/ventas/${id}`);
    ventaActual = venta;
    renderizarDetalleVenta(venta);
  } catch (error) {
    contenedor.innerHTML = `<div class="alert alert-danger mb-0">${escaparHtml(error.message)}</div>`;
  }
}



function renderizarDetalleVenta(venta) {
  const contenedor = document.getElementById("contenedorDetalleVenta");
  const claseBadge = { PENDIENTE: "badge-pendiente", COMPLETADA: "badge-completada", ANULADA: "badge-anulada" }[venta.estado];
  const esPendiente = venta.estado === "PENDIENTE";
  const esCompletada = venta.estado === "COMPLETADA";

  contenedor.innerHTML = `
    <div class="d-flex justify-content-between align-items-start mb-3">
      <div>
        <h3 class="h6 fw-bold mb-0">Venta #${venta.id}</h3>
        <p class="text-muted small mb-0">${formatearFechaHora(venta.fechaCreacion)}</p>
      </div>
      <span class="badge-estado ${claseBadge}">${traducirEstadoVenta(venta.estado)}</span>
    </div>

    <p class="mb-1"><i class="bi bi-person"></i> <strong>${escaparHtml(venta.nombreCliente)}</strong></p>
    <p class="text-muted small mb-3"><i class="bi bi-person-badge"></i> Vendedor: ${escaparHtml(venta.correoVendedor)}</p>

    <div class="table-responsive mb-3">
      <table class="table table-sm align-middle">
        <thead>
          <tr><th>Producto</th><th class="text-center">Cant.</th><th class="text-end">Subtotal</th></tr>
        </thead>
        <tbody>
          ${venta.detalles
            .map(
              (detalle) => `
              <tr>
                <td>${escaparHtml(detalle.nombreProducto)}</td>
                <td class="text-center">${detalle.cantidad}</td>
                <td class="text-end">${formatearMoneda(detalle.totalLinea)}</td>
              </tr>
            `
            )
            .join("")}
        </tbody>
      </table>
    </div>

    <div class="resumen-totales mb-3">
      <div class="fila-total"><span>Subtotal</span><span>${formatearMoneda(venta.subtotal)}</span></div>
      <div class="fila-total"><span>IVA</span><span>${formatearMoneda(venta.iva)}</span></div>
      <div class="fila-total total-final"><span>Total</span><span>${formatearMoneda(venta.total)}</span></div>
    </div>

    <div class="d-flex flex-wrap gap-2">
      <button type="button" class="btn btn-success btn-sm" id="botonCompletarVenta" ${esPendiente ? "" : "disabled"}>
        <i class="bi bi-check-lg"></i> Completar venta
      </button>
      <button type="button" class="btn btn-outline-danger btn-sm" id="botonAnularVenta" ${esPendiente ? "" : "disabled"}>
        <i class="bi bi-x-lg"></i> Anular venta
      </button>
      <button type="button" class="btn btn-outline-dark btn-sm" id="botonVerFactura" ${esCompletada ? "" : "disabled"}>
        <i class="bi bi-file-earmark-text"></i> Ver factura
      </button>
    </div>
    ${!esCompletada ? '<p class="text-muted small mt-2 mb-0">La factura solo está disponible una vez que la venta se completa.</p>' : ""}
  `;

  const botonCompletar = document.getElementById("botonCompletarVenta");
  const botonAnular = document.getElementById("botonAnularVenta");
  const botonFactura = document.getElementById("botonVerFactura");

  if (botonCompletar) botonCompletar.addEventListener("click", () => cambiarEstadoVenta(venta.id, "completar"));
  if (botonAnular) botonAnular.addEventListener("click", () => cambiarEstadoVenta(venta.id, "anular"));
  if (botonFactura) botonFactura.addEventListener("click", () => verFactura(venta.id));
}

async function cambiarEstadoVenta(id, accion) {
  const mensajeConfirmacion =
    accion === "completar"
      ? "¿Completar esta venta? Se descontará el stock de los productos y se generará la factura."
      : "¿Anular esta venta? Esta acción no se puede deshacer.";

  if (!window.confirm(mensajeConfirmacion)) return;

  try {
    const venta = await solicitarApi(`/ventas/${id}/${accion}`, { method: "POST" });
    ventaActual = venta;
    renderizarDetalleVenta(venta);
    mostrarAlerta(
      document.getElementById("contenedorAlerta"),
      accion === "completar" ? `Venta #${venta.id} completada. Stock actualizado y factura generada.` : `Venta #${venta.id} anulada.`,
      "success"
    );
    await cargarDatosIniciales();
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message || "No se pudo actualizar la venta.");
  }
}



async function verFactura(ventaId) {
  const cuerpo = document.getElementById("cuerpoModalFactura");
  cuerpo.innerHTML = `<div class="text-center text-muted py-4"><div class="spinner-border spinner-border-sm me-2"></div> Cargando factura...</div>`;
  new bootstrap.Modal(document.getElementById("modalFactura")).show();

  try {
    const factura = await solicitarApi(`/ventas/${ventaId}/factura`);
    renderizarFactura(factura, cuerpo);
  } catch (error) {
    cuerpo.innerHTML = `<div class="alert alert-danger mb-0">${escaparHtml(error.message)}</div>`;
  }
}

function renderizarFactura(factura, contenedor) {
  const venta = factura.venta;
  contenedor.innerHTML = `
    <div class="factura-encabezado d-flex justify-content-between align-items-start">
      <div>
        <p class="mb-1 text-muted small">Número de factura</p>
        <p class="numero-factura mb-0">${escaparHtml(factura.numeroFactura)}</p>
      </div>
      <div class="text-end">
        <p class="mb-1 text-muted small">Fecha de emisión</p>
        <p class="mb-0">${formatearFechaHora(factura.fechaEmision)}</p>
      </div>
    </div>

    <p class="mb-1"><strong>Cliente:</strong> ${escaparHtml(venta.nombreCliente)}</p>
    <p class="mb-3 text-muted small"><strong>Vendedor:</strong> ${escaparHtml(venta.correoVendedor)}</p>

    <div class="table-responsive mb-3">
      <table class="table table-sm align-middle">
        <thead>
          <tr><th>Producto</th><th class="text-center">Cant.</th><th class="text-end">Precio</th><th class="text-end">Subtotal</th></tr>
        </thead>
        <tbody>
          ${venta.detalles
            .map(
              (detalle) => `
              <tr>
                <td>${escaparHtml(detalle.nombreProducto)}</td>
                <td class="text-center">${detalle.cantidad}</td>
                <td class="text-end">${formatearMoneda(detalle.precioUnitario)}</td>
                <td class="text-end">${formatearMoneda(detalle.totalLinea)}</td>
              </tr>
            `
            )
            .join("")}
        </tbody>
      </table>
    </div>

    <div class="resumen-totales">
      <div class="fila-total"><span>Subtotal</span><span>${formatearMoneda(venta.subtotal)}</span></div>
      <div class="fila-total"><span>IVA</span><span>${formatearMoneda(venta.iva)}</span></div>
      <div class="fila-total total-final"><span>Total</span><span>${formatearMoneda(venta.total)}</span></div>
    </div>
  `;
}

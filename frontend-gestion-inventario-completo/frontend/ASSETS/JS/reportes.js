

document.addEventListener("DOMContentLoaded", () => {
  const usuario = inicializarNavbar();

  if (!esUsuarioAdmin(usuario)) {
    document.getElementById("contenedorSinPermiso").classList.remove("d-none");
    return;
  }

  document.getElementById("contenedorReportes").classList.remove("d-none");

  const hoy = new Date();
  document.getElementById("fechaVentasDiarias").value = formatearFechaInput(hoy);
  document.getElementById("anioVentasMensuales").value = hoy.getFullYear();
  document.getElementById("mesVentasMensuales").value = hoy.getMonth() + 1;

  const haceUnMes = new Date(hoy);
  haceUnMes.setDate(haceUnMes.getDate() - 30);
  document.getElementById("fechaDesdeMasVendidos").value = formatearFechaInput(haceUnMes);
  document.getElementById("fechaHastaMasVendidos").value = formatearFechaInput(hoy);

  document.getElementById("botonConsultarDiarias").addEventListener("click", consultarVentasDiarias);
  document.getElementById("botonConsultarMensuales").addEventListener("click", consultarVentasMensuales);
  document.getElementById("botonConsultarMasVendidos").addEventListener("click", consultarMasVendidos);

  cargarEstadisticasGenerales();
  consultarVentasDiarias();
  consultarVentasMensuales();
  consultarMasVendidos();
  cargarReporteInventario();
});

function formatearFechaInput(fecha) {
  return fecha.toISOString().slice(0, 10);
}

async function cargarEstadisticasGenerales() {
  try {
    const estadisticas = await solicitarApi("/panel/estadisticas");
    document.getElementById("kpiVentasMes").textContent = formatearMoneda(estadisticas.totalVentasDelMes);
    document.getElementById("kpiProductosActivos").textContent = estadisticas.cantidadProductosActivos;
    document.getElementById("kpiProductosAgotados").textContent = estadisticas.cantidadProductosAgotados;
    document.getElementById("kpiClientes").textContent = estadisticas.cantidadClientesRegistrados;
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message, "danger");
  }
}



async function consultarVentasDiarias() {
  const contenedor = document.getElementById("resultadoVentasDiarias");
  const fecha = document.getElementById("fechaVentasDiarias").value;
  if (!fecha) return;

  contenedor.innerHTML = `<div class="text-center text-muted py-3"><div class="spinner-border spinner-border-sm me-2"></div> Consultando...</div>`;

  try {
    const reporte = await solicitarApi(`/reportes/ventas-diarias?fecha=${fecha}`);
    renderizarResumenVentas(reporte, contenedor);
  } catch (error) {
    contenedor.innerHTML = `<div class="alert alert-danger">${escaparHtml(error.message)}</div>`;
  }
}



async function consultarVentasMensuales() {
  const contenedor = document.getElementById("resultadoVentasMensuales");
  const anio = Number(document.getElementById("anioVentasMensuales").value);
  const mes = Number(document.getElementById("mesVentasMensuales").value);
  if (!anio || !mes) return;

  contenedor.innerHTML = `<div class="text-center text-muted py-3"><div class="spinner-border spinner-border-sm me-2"></div> Consultando...</div>`;

  try {
    const reporte = await solicitarApi(`/reportes/ventas-mensuales?anio=${anio}&mes=${mes}`);
    renderizarResumenVentas(reporte, contenedor);
  } catch (error) {
    contenedor.innerHTML = `<div class="alert alert-danger">${escaparHtml(error.message)}</div>`;
  }
}

function renderizarResumenVentas(reporte, contenedor) {
  contenedor.innerHTML = `
    <div class="row g-3">
      <div class="col-6 col-md-4">
        <div class="tarjeta-kpi">
          <p class="kpi-etiqueta mb-1">Periodo</p>
          <p class="kpi-valor mb-0" style="font-size:1.1rem">${escaparHtml(reporte.etiquetaPeriodo)}</p>
        </div>
      </div>
      <div class="col-6 col-md-4">
        <div class="tarjeta-kpi">
          <p class="kpi-etiqueta mb-1">Total en ventas</p>
          <p class="kpi-valor mb-0">${formatearMoneda(reporte.totalVentas)}</p>
        </div>
      </div>
      <div class="col-6 col-md-4">
        <div class="tarjeta-kpi">
          <p class="kpi-etiqueta mb-1">Transacciones</p>
          <p class="kpi-valor mb-0">${reporte.cantidadTransacciones}</p>
        </div>
      </div>
    </div>
  `;
}



async function consultarMasVendidos() {
  const cuerpoTabla = document.getElementById("cuerpoTablaMasVendidos");
  const desde = document.getElementById("fechaDesdeMasVendidos").value;
  const hasta = document.getElementById("fechaHastaMasVendidos").value;
  if (!desde || !hasta) return;

  cuerpoTabla.innerHTML = `<tr><td colspan="3" class="text-center text-muted py-4"><div class="spinner-border spinner-border-sm me-2"></div> Consultando...</td></tr>`;

  try {
    const productos = await solicitarApi(`/reportes/productos-mas-vendidos?desde=${desde}&hasta=${hasta}`);

    if (!productos || productos.length === 0) {
      cuerpoTabla.innerHTML = `<tr><td colspan="3"><div class="pila-vacia"><i class="bi bi-inbox d-block mb-2"></i>No hay ventas en el rango seleccionado.</div></td></tr>`;
      return;
    }

    cuerpoTabla.innerHTML = productos
      .map(
        (producto, indice) => `
          <tr>
            <td class="fw-bold">${indice + 1}</td>
            <td>${escaparHtml(producto.nombreProducto)}</td>
            <td class="text-end">${producto.cantidadTotalVendida}</td>
          </tr>
        `
      )
      .join("");
  } catch (error) {
    cuerpoTabla.innerHTML = `<tr><td colspan="3" class="text-center text-danger py-4">${escaparHtml(error.message)}</td></tr>`;
  }
}



async function cargarReporteInventario() {
  const cuerpoTabla = document.getElementById("cuerpoTablaInventario");

  try {
    const items = await solicitarApi("/reportes/inventario");

    if (!items || items.length === 0) {
      cuerpoTabla.innerHTML = `<tr><td colspan="5"><div class="pila-vacia"><i class="bi bi-inbox d-block mb-2"></i>No hay productos registrados.</div></td></tr>`;
      return;
    }

    cuerpoTabla.innerHTML = items
      .map(
        (item) => `
          <tr>
            <td class="codigo-producto">${escaparHtml(item.codigo)}</td>
            <td class="fw-semibold">${escaparHtml(item.nombreProducto)}</td>
            <td class="text-end">${item.stockActual}</td>
            <td class="text-end">${item.stockMinimo}</td>
            <td class="text-center">
              ${item.stockBajo ? '<span class="badge-estado badge-stock-bajo">Stock bajo</span>' : '<span class="badge-estado badge-disponible">Normal</span>'}
            </td>
          </tr>
        `
      )
      .join("");
  } catch (error) {
    cuerpoTabla.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">${escaparHtml(error.message)}</td></tr>`;
  }
}

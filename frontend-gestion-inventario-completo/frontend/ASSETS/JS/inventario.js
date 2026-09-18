

let listaProductosInventario = [];

document.addEventListener("DOMContentLoaded", () => {
  inicializarNavbar();

  configurarFormularioMovimiento();
  configurarSelectHistorial();
  cargarProductos();
});

async function cargarProductos() {
  try {
    listaProductosInventario = await solicitarApi("/productos");
    poblarSelectsProducto();
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlerta"), error.message, "danger");
  }
}

function poblarSelectsProducto() {
  const opciones = listaProductosInventario
    .map((producto) => `<option value="${producto.id}">${escaparHtml(producto.codigo)} · ${escaparHtml(producto.nombre)} (stock: ${producto.stockActual})</option>`)
    .join("");

  const selectMovimiento = document.getElementById("productoMovimiento");
  selectMovimiento.innerHTML = '<option value="" selected disabled>Selecciona un producto</option>' + opciones;

  const selectHistorial = document.getElementById("productoHistorial");
  selectHistorial.innerHTML = '<option value="">Selecciona un producto para ver su historial</option>' + opciones;
}



function configurarFormularioMovimiento() {
  const formulario = document.getElementById("formularioMovimiento");

  document.getElementById("productoMovimiento").addEventListener("change", mostrarInfoProductoSeleccionado);

  formulario.addEventListener("submit", manejarEnvioMovimiento);
}

function mostrarInfoProductoSeleccionado() {
  const id = Number(document.getElementById("productoMovimiento").value);
  const producto = listaProductosInventario.find((item) => item.id === id);
  const contenedor = document.getElementById("infoProductoSeleccionado");

  if (!producto) {
    contenedor.classList.add("d-none");
    return;
  }

  contenedor.classList.remove("d-none");
  contenedor.innerHTML = `
    <strong>Stock actual:</strong> ${producto.stockActual} &nbsp;·&nbsp;
    <strong>Stock mínimo:</strong> ${producto.stockMinimo} &nbsp;·&nbsp;
    <strong>Precio:</strong> ${formatearMoneda(producto.precio)}
  `;
}

async function manejarEnvioMovimiento(evento) {
  evento.preventDefault();
  document.getElementById("errorProductoMovimiento").textContent = "";
  document.getElementById("errorCantidadMovimiento").textContent = "";
  limpiarAlerta(document.getElementById("contenedorAlertaFormulario"));

  const productoId = Number(document.getElementById("productoMovimiento").value) || null;
  const tipo = document.querySelector('input[name="tipoMovimiento"]:checked').value;
  const cantidad = Number(document.getElementById("cantidadMovimiento").value);
  const motivo = document.getElementById("motivoMovimiento").value.trim() || null;

  let esValido = true;
  if (!productoId) {
    document.getElementById("errorProductoMovimiento").textContent = "Selecciona un producto.";
    esValido = false;
  }
  if (!cantidad || cantidad <= 0) {
    document.getElementById("errorCantidadMovimiento").textContent = "La cantidad debe ser mayor a 0.";
    esValido = false;
  }
  if (!esValido) return;

  const boton = document.getElementById("botonRegistrarMovimiento");
  const spinner = document.getElementById("spinnerMovimiento");
  const texto = document.getElementById("textoBotonMovimiento");

  boton.disabled = true;
  spinner.classList.remove("d-none");
  texto.textContent = "Registrando...";

  try {
    await solicitarApi("/inventario/movimientos", {
      method: "POST",
      body: JSON.stringify({ productoId, tipo, cantidad, motivo }),
    });

    mostrarAlerta(document.getElementById("contenedorAlerta"), "Movimiento registrado correctamente. El stock del producto se actualizó.", "success");
    document.getElementById("formularioMovimiento").reset();
    document.getElementById("infoProductoSeleccionado").classList.add("d-none");

    await cargarProductos();

    // Si el historial visible corresponde al producto afectado, se refresca.
    const historialActual = document.getElementById("productoHistorial").value;
    if (historialActual && Number(historialActual) === productoId) {
      document.getElementById("productoHistorial").value = String(productoId);
      await cargarHistorial(productoId);
    }
  } catch (error) {
    mostrarAlerta(document.getElementById("contenedorAlertaFormulario"), error.message || "No se pudo registrar el movimiento.");
  } finally {
    boton.disabled = false;
    spinner.classList.add("d-none");
    texto.textContent = "Registrar movimiento";
  }
}


function configurarSelectHistorial() {
  document.getElementById("productoHistorial").addEventListener("change", (evento) => {
    const id = evento.target.value;
    if (id) {
      cargarHistorial(Number(id));
    } else {
      document.getElementById("cuerpoTablaMovimientos").innerHTML = `
        <tr><td colspan="6" class="text-center text-muted py-4">Selecciona un producto para ver su historial de movimientos.</td></tr>
      `;
    }
  });
}

async function cargarHistorial(productoId) {
  const cuerpoTabla = document.getElementById("cuerpoTablaMovimientos");
  cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4"><div class="spinner-border spinner-border-sm me-2"></div> Cargando historial...</td></tr>`;

  try {
    const movimientos = await solicitarApi(`/inventario/movimientos/producto/${productoId}`);
    renderizarHistorial(movimientos);
  } catch (error) {
    cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">${escaparHtml(error.message)}</td></tr>`;
  }
}

function renderizarHistorial(movimientos) {
  const cuerpoTabla = document.getElementById("cuerpoTablaMovimientos");

  if (!movimientos || movimientos.length === 0) {
    cuerpoTabla.innerHTML = `
      <tr>
        <td colspan="6">
          <div class="pila-vacia">
            <i class="bi bi-inbox d-block mb-2"></i>
            Este producto aún no tiene movimientos registrados.
          </div>
        </td>
      </tr>
    `;
    return;
  }

  cuerpoTabla.innerHTML = movimientos
    .map((movimiento) => {
      const claseBadge = movimiento.tipo === "ENTRADA" ? "badge-entrada" : "badge-salida";
      const signo = movimiento.tipo === "ENTRADA" ? "+" : "-";
      return `
        <tr>
          <td>${formatearFechaHora(movimiento.fecha)}</td>
          <td class="text-center"><span class="badge-estado ${claseBadge}">${traducirTipoMovimiento(movimiento.tipo)}</span></td>
          <td class="text-end fw-semibold">${signo}${movimiento.cantidad}</td>
          <td class="text-end">${movimiento.stockResultante}</td>
          <td>${movimiento.motivo ? escaparHtml(movimiento.motivo) : '<span class="text-muted">-</span>'}</td>
          <td>${escaparHtml(movimiento.correoUsuario)}</td>
        </tr>
      `;
    })
    .join("");
}

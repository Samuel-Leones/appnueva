
document.addEventListener("DOMContentLoaded", () => {
  const usuario = inicializarNavbar();

  if (!esUsuarioAdmin(usuario)) {
    document.getElementById("contenedorSinPermiso").classList.remove("d-none");
    return;
  }

  document.getElementById("contenedorAuditoria").classList.remove("d-none");

  document.getElementById("filtroEntidad").addEventListener("change", (evento) => {
    cargarRegistros(evento.target.value || null);
  });

  cargarRegistros(null);
});

async function cargarRegistros(entidad) {
  const contenedor = document.getElementById("lineaTiempoAuditoria");
  contenedor.innerHTML = `<div class="text-center text-muted py-4"><div class="spinner-border spinner-border-sm me-2"></div> Cargando registros...</div>`;

  try {
    const ruta = entidad ? `/auditoria?entidad=${encodeURIComponent(entidad)}` : "/auditoria";
    const registros = await solicitarApi(ruta);
    renderizarLineaTiempo(registros);
  } catch (error) {
    contenedor.innerHTML = `<div class="alert alert-danger">${escaparHtml(error.message)}</div>`;
  }
}

function renderizarLineaTiempo(registros) {
  const contenedor = document.getElementById("lineaTiempoAuditoria");
  const contador = document.getElementById("contadorResultados");

  contador.textContent = `${registros.length} registro(s) encontrado(s)`;

  if (!registros || registros.length === 0) {
    contenedor.innerHTML = `
      <div class="pila-vacia">
        <i class="bi bi-inbox d-block mb-2"></i>
        No hay registros de auditoría para este filtro.
      </div>
    `;
    return;
  }

  const ordenados = [...registros].sort((a, b) => new Date(b.fechaHora) - new Date(a.fechaHora));

  const claseBadge = { CREACION: "badge-creacion", ACTUALIZACION: "badge-actualizacion", ELIMINACION: "badge-eliminacion" };
  const iconoAccion = { CREACION: "bi-plus-circle", ACTUALIZACION: "bi-pencil-square", ELIMINACION: "bi-trash" };

  contenedor.innerHTML = ordenados
    .map(
      (registro) => `
        <div class="linea-tiempo-item">
          <div class="d-flex flex-wrap justify-content-between align-items-start gap-2">
            <div>
              <span class="badge-estado ${claseBadge[registro.accion]}">
                <i class="bi ${iconoAccion[registro.accion]}"></i> ${traducirAccionAuditoria(registro.accion)}
              </span>
              <span class="fw-semibold ms-2">${escaparHtml(registro.entidadAfectada)} #${registro.idEntidadAfectada}</span>
            </div>
            <span class="text-muted small">${formatearFechaHora(registro.fechaHora)}</span>
          </div>
          <p class="text-muted small mb-0 mt-1"><i class="bi bi-person"></i> ${escaparHtml(registro.correoUsuario)}</p>
        </div>
      `
    )
    .join("");
}

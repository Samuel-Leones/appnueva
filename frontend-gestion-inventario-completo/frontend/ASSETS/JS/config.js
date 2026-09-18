
const API_BASE_URL = "http://localhost:8080";

const CLAVE_TOKEN = "tokenAcceso";
const CLAVE_TIPO_TOKEN = "tipoToken";

/* ---------------------- Sesión / token ---------------------- */

/** Guarda el token de sesión en localStorage; persiste hasta que el usuario cierre sesión. */
function guardarSesion(tokenAcceso, tipoToken) {
  limpiarSesion();
  localStorage.setItem(CLAVE_TOKEN, tokenAcceso);
  localStorage.setItem(CLAVE_TIPO_TOKEN, tipoToken || "Bearer");
}

function obtenerToken() {
  return localStorage.getItem(CLAVE_TOKEN) || sessionStorage.getItem(CLAVE_TOKEN);
}

function obtenerTipoToken() {
  return localStorage.getItem(CLAVE_TIPO_TOKEN) || sessionStorage.getItem(CLAVE_TIPO_TOKEN) || "Bearer";
}

function limpiarSesion() {
  localStorage.removeItem(CLAVE_TOKEN);
  localStorage.removeItem(CLAVE_TIPO_TOKEN);
  sessionStorage.removeItem(CLAVE_TOKEN);
  sessionStorage.removeItem(CLAVE_TIPO_TOKEN);
}

function haySesionActiva() {
  const token = obtenerToken();
  return !!token && !tokenExpirado(token);
}


function cerrarSesion() {
  limpiarSesion();
  redirigirALogin("sesion_cerrada");
}


function redirigirALogin(motivo) {
  const destino = motivo ? `Login.html?mensaje=${motivo}` : "Login.html";
  // replace() evita que la página protegida quede en el historial de navegación.
  window.location.replace(destino);
}


function decodificarToken(token) {
  try {
    const payload = token.split(".")[1];
    const json = decodeURIComponent(
      atob(payload.replace(/-/g, "+").replace(/_/g, "/"))
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(json);
  } catch (error) {
    return null;
  }
}

function tokenExpirado(token) {
  const datos = decodificarToken(token);
  if (!datos || !datos.exp) return true;
  return Date.now() >= datos.exp * 1000;
}


function obtenerUsuarioActual() {
  const token = obtenerToken();
  if (!token) return null;
  const datos = decodificarToken(token);
  if (!datos) return null;
  return {
    correo: datos.sub,
    rol: datos.rol,
    usuarioId: datos.usuarioId,
  };
}


function requerirSesion() {
  if (!haySesionActiva()) {
    limpiarSesion();
    redirigirALogin("sesion_requerida");
  }
}


function protegerContraCacheDeNavegacion() {
  window.addEventListener("pageshow", (evento) => {
    const restauradaDesdeCache =
      evento.persisted ||
      (window.performance && performance.getEntriesByType("navigation")[0]?.type === "back_forward");

    if (restauradaDesdeCache) {
      requerirSesion();
    }
  });
}




function encabezadosAutorizacion() {
  const token = obtenerToken();
  return token ? { Authorization: `${obtenerTipoToken()} ${token}` } : {};
}


async function solicitarApi(ruta, opciones = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...encabezadosAutorizacion(),
    ...(opciones.headers || {}),
  };

  let respuesta;
  try {
    respuesta = await fetch(`${API_BASE_URL}${ruta}`, { ...opciones, headers });
  } catch (error) {
    throw new Error("No se pudo conectar con el servidor. Verifica que el backend esté encendido.");
  }

  if (respuesta.status === 401) {
    limpiarSesion();
    redirigirALogin("sesion_expirada");
    throw new Error("Sesión expirada. Inicia sesión nuevamente.");
  }

  if (respuesta.status === 204) {
    return null;
  }

  const esJson = (respuesta.headers.get("content-type") || "").includes("application/json");
  const cuerpo = esJson ? await respuesta.json().catch(() => null) : null;

  if (!respuesta.ok) {
    const mensaje =
      (cuerpo && cuerpo.message) ||
      mensajePorCodigo(respuesta.status);
    throw new Error(mensaje);
  }

  return cuerpo;
}

function mensajePorCodigo(status) {
  switch (status) {
    case 400:
      return "La información enviada no es válida.";
    case 403:
      return "No tienes permisos para realizar esta acción.";
    case 404:
      return "El recurso solicitado no existe.";
    case 409:
      return "Ya existe un registro con esos datos.";
    case 500:
      return "Ocurrió un error interno en el servidor.";
    default:
      return "Ocurrió un error inesperado.";
  }
}



function mostrarAlerta(contenedor, mensaje, tipo = "danger") {
  if (!contenedor) return;
  contenedor.innerHTML = `
    <div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
      ${mensaje}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
    </div>
  `;
}

function limpiarAlerta(contenedor) {
  if (contenedor) contenedor.innerHTML = "";
}


function formatearMoneda(valor) {
  const numero = Number(valor) || 0;
  return numero.toLocaleString("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });
}

function formatearFecha(valorIso) {
  if (!valorIso) return "-";
  const fecha = new Date(valorIso);
  if (Number.isNaN(fecha.getTime())) return "-";
  return fecha.toLocaleDateString("es-CO", { year: "numeric", month: "short", day: "2-digit" });
}

function escaparHtml(texto) {
  const div = document.createElement("div");
  div.textContent = texto ?? "";
  return div.innerHTML;
}



const ROLES_ADMIN = ["ADMINISTRADOR", "SUPER_USUARIO"];

function traducirRol(rol) {
  const nombres = {
    SUPER_USUARIO: "Super usuario",
    ADMINISTRADOR: "Administrador",
    EMPLEADO: "Empleado",
  };
  return nombres[rol] || rol || "-";
}


 
function inicializarNavbar() {
  requerirSesion();
  protegerContraCacheDeNavegacion();
  const usuario = obtenerUsuarioActual();

  const nombre = document.getElementById("nombreUsuarioNavbar");
  const rol = document.getElementById("rolUsuarioNavbar");
  if (usuario) {
    if (nombre) nombre.textContent = usuario.correo;
    if (rol) rol.textContent = traducirRol(usuario.rol);
  }

  const botonCerrarSesion = document.getElementById("botonCerrarSesion");
  if (botonCerrarSesion) botonCerrarSesion.addEventListener("click", cerrarSesion);

  const esAdmin = usuario && ROLES_ADMIN.includes(usuario.rol);
  ["itemRegistrarUsuario", "navReportes", "navAuditoria"].forEach((id) => {
    const elemento = document.getElementById(id);
    if (elemento && !esAdmin) elemento.classList.add("d-none");
  });

  return usuario;
}

function esUsuarioAdmin(usuario) {
  return !!usuario && ROLES_ADMIN.includes(usuario.rol);
}



function traducirEstadoVenta(estado) {
  return { PENDIENTE: "Pendiente", COMPLETADA: "Completada", ANULADA: "Anulada" }[estado] || estado;
}

function traducirEstadoCategoria(estado) {
  return { PENDIENTE: "Pendiente", ACTIVA: "Activa", ELIMINADA: "Eliminada" }[estado] || estado;
}

function traducirTipoMovimiento(tipo) {
  return { ENTRADA: "Entrada", SALIDA: "Salida" }[tipo] || tipo;
}

function traducirAccionAuditoria(accion) {
  return { CREACION: "Creación", ACTUALIZACION: "Actualización", ELIMINACION: "Eliminación" }[accion] || accion;
}

function formatearFechaHora(valorIso) {
  if (!valorIso) return "-";
  const fecha = new Date(valorIso);
  if (Number.isNaN(fecha.getTime())) return "-";
  return fecha.toLocaleString("es-CO", {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

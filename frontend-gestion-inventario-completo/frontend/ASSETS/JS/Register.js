
const ROLES_CON_PERMISO_REGISTRO = ["ADMINISTRADOR", "SUPER_USUARIO"];

document.addEventListener("DOMContentLoaded", () => {
  requerirSesion();

  const formulario = document.getElementById("formularioRegistro");
  const contenedorAlerta = document.getElementById("contenedorAlerta");
  const avisoSinPermiso = document.getElementById("avisoSinPermiso");

  const campoNombre = document.getElementById("nombreCompleto");
  const campoCorreo = document.getElementById("correoRegistro");
  const campoRol = document.getElementById("rol");
  const campoContrasena = document.getElementById("contrasenaRegistro");
  const campoConfirmar = document.getElementById("confirmarContrasena");

  const errorNombre = document.getElementById("errorNombreCompleto");
  const errorCorreo = document.getElementById("errorCorreoRegistro");
  const errorRol = document.getElementById("errorRol");
  const errorContrasena = document.getElementById("errorContrasenaRegistro");
  const errorConfirmar = document.getElementById("errorConfirmarContrasena");

  const botonRegistro = document.getElementById("botonRegistro");
  const spinnerRegistro = document.getElementById("spinnerRegistro");
  const textoBotonRegistro = document.getElementById("textoBotonRegistro");

  const botonMostrarContrasena = document.getElementById("botonMostrarContrasenaRegistro");
  const iconoMostrarContrasena = document.getElementById("iconoMostrarContrasenaRegistro");

  const rellenoFortaleza = document.getElementById("rellenoFortaleza");
  const textoFortaleza = document.getElementById("textoFortaleza");

  const usuario = obtenerUsuarioActual();
  const tienePermiso = usuario && ROLES_CON_PERMISO_REGISTRO.includes(usuario.rol);

  if (!tienePermiso) {
    formulario.classList.add("d-none");
    avisoSinPermiso.classList.remove("d-none");
    avisoSinPermiso.innerHTML = `
      <div class="alert alert-warning">
        <i class="bi bi-shield-exclamation"></i>
        Tu cuenta (rol <strong>${escaparHtml(usuario ? usuario.rol : "desconocido")}</strong>)
        no tiene permisos para registrar nuevos usuarios. Esta acción solo está disponible
        para administradores.
      </div>
    `;
    return;
  }

  botonMostrarContrasena.addEventListener("click", () => {
    alternarVisibilidadContrasena(campoContrasena, iconoMostrarContrasena);
  });

  campoContrasena.addEventListener("input", () => {
    actualizarFortaleza(campoContrasena.value, rellenoFortaleza, textoFortaleza);
  });

  formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limpiarAlerta(contenedorAlerta);
    [errorNombre, errorCorreo, errorRol, errorContrasena, errorConfirmar].forEach((el) => (el.textContent = ""));

    const datos = {
      nombreCompleto: campoNombre.value.trim(),
      correo: campoCorreo.value.trim(),
      rol: campoRol.value,
      contrasena: campoContrasena.value,
    };

    if (!validarRegistro(datos, campoConfirmar.value, { errorNombre, errorCorreo, errorRol, errorContrasena, errorConfirmar })) {
      return;
    }

    alternarCargando(true);

    try {
      await solicitarApi("/autenticacion/registro", {
        method: "POST",
        body: JSON.stringify(datos),
      });

      mostrarAlerta(contenedorAlerta, `Usuario <strong>${escaparHtml(datos.correo)}</strong> registrado correctamente.`, "success");
      formulario.reset();
      rellenoFortaleza.style.width = "0%";
      textoFortaleza.textContent = "Fortaleza de la contraseña";
    } catch (error) {
      mostrarAlerta(contenedorAlerta, error.message || "No se pudo registrar el usuario.");
    } finally {
      alternarCargando(false);
    }
  });

  function alternarCargando(cargando) {
    botonRegistro.disabled = cargando;
    spinnerRegistro.classList.toggle("d-none", !cargando);
    textoBotonRegistro.textContent = cargando ? "Registrando..." : "Registrar usuario";
  }
});

function validarRegistro(datos, confirmacion, campos) {
  let esValido = true;
  const expresionCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!datos.nombreCompleto) {
    campos.errorNombre.textContent = "El nombre completo es obligatorio.";
    esValido = false;
  }

  if (!datos.correo) {
    campos.errorCorreo.textContent = "El correo es obligatorio.";
    esValido = false;
  } else if (!expresionCorreo.test(datos.correo)) {
    campos.errorCorreo.textContent = "Ingresa un correo válido.";
    esValido = false;
  }

  if (!datos.rol) {
    campos.errorRol.textContent = "Selecciona un rol.";
    esValido = false;
  }

  if (!datos.contrasena || datos.contrasena.length < 8) {
    campos.errorContrasena.textContent = "La contraseña debe tener al menos 8 caracteres.";
    esValido = false;
  }

  if (datos.contrasena !== confirmacion) {
    campos.errorConfirmar.textContent = "Las contraseñas no coinciden.";
    esValido = false;
  }

  return esValido;
}

function actualizarFortaleza(contrasena, relleno, texto) {
  let puntaje = 0;
  if (contrasena.length >= 8) puntaje++;
  if (/[A-Z]/.test(contrasena)) puntaje++;
  if (/[0-9]/.test(contrasena)) puntaje++;
  if (/[^A-Za-z0-9]/.test(contrasena)) puntaje++;

  const niveles = [
    { ancho: "0%", color: "#e5e7eb", texto: "Fortaleza de la contraseña" },
    { ancho: "25%", color: "#d62828", texto: "Débil" },
    { ancho: "50%", color: "#e07a1a", texto: "Aceptable" },
    { ancho: "75%", color: "#e0c11a", texto: "Buena" },
    { ancho: "100%", color: "#1f9d55", texto: "Fuerte" },
  ];

  const nivel = niveles[contrasena ? puntaje : 0];
  relleno.style.width = nivel.ancho;
  relleno.style.backgroundColor = nivel.color;
  texto.textContent = nivel.texto;
}

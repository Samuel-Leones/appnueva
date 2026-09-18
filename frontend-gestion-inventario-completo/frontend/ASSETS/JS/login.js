
document.addEventListener("DOMContentLoaded", () => {

  if (haySesionActiva()) {
    window.location.href = "tasks.html";
    return;
  }

  const formulario = document.getElementById("formularioLogin");
  const campoCorreo = document.getElementById("correo");
  const campoContrasena = document.getElementById("contrasena");
  const contenedorAlerta = document.getElementById("contenedorAlerta");
  const botonLogin = document.getElementById("botonLogin");
  const spinnerLogin = document.getElementById("spinnerLogin");
  const textoBotonLogin = document.getElementById("textoBotonLogin");
  const errorCorreo = document.getElementById("errorCorreo");
  const errorContrasena = document.getElementById("errorContrasena");

  mostrarMensajeDeRedireccion(contenedorAlerta);

  const botonMostrarContrasena = document.getElementById("botonMostrarContrasena");
  const iconoMostrarContrasena = document.getElementById("iconoMostrarContrasena");

  botonMostrarContrasena.addEventListener("click", () => {
    alternarVisibilidadContrasena(campoContrasena, iconoMostrarContrasena);
  });

  formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limpiarAlerta(contenedorAlerta);
    errorCorreo.textContent = "";
    errorContrasena.textContent = "";

    const correo = campoCorreo.value.trim();
    const contrasena = campoContrasena.value;

    if (!validarFormulario(correo, contrasena, errorCorreo, errorContrasena)) {
      return;
    }

    alternarCargando(true);

    try {
      const respuesta = await solicitarApi("/autenticacion/login", {
        method: "POST",
        body: JSON.stringify({ correo, contrasena }),
      });

      guardarSesion(respuesta.tokenAcceso, respuesta.tipoToken);

      mostrarAlerta(contenedorAlerta, "<i class=\"bi bi-check-circle\"></i> Sesión iniciada correctamente. Redirigiendo...", "success");
      textoBotonLogin.textContent = "¡Listo!";

    
      setTimeout(() => {
        window.location.replace("tasks.html");
      }, 700);
    } catch (error) {
      mostrarAlerta(contenedorAlerta, error.message || "No se pudo iniciar sesión.");
      alternarCargando(false);
    }
  });

  function alternarCargando(cargando) {
    botonLogin.disabled = cargando;
    spinnerLogin.classList.toggle("d-none", !cargando);
    textoBotonLogin.textContent = cargando ? "Ingresando..." : "Iniciar sesión";
  }
});


function mostrarMensajeDeRedireccion(contenedorAlerta) {
  const parametros = new URLSearchParams(window.location.search);
  const motivo = parametros.get("mensaje");
  if (!motivo) return;

  const mensajes = {
    sesion_cerrada: { texto: "Sesión cerrada correctamente.", tipo: "success" },
    sesion_requerida: { texto: "Debes iniciar sesión para continuar.", tipo: "info" },
    sesion_expirada: { texto: "Tu sesión expiró. Inicia sesión nuevamente.", tipo: "warning" },
  };

  const mensaje = mensajes[motivo];
  if (mensaje) {
    mostrarAlerta(contenedorAlerta, mensaje.texto, mensaje.tipo);
  }

  
  window.history.replaceState({}, document.title, "Login.html");
}


function validarFormulario(correo, contrasena, errorCorreo, errorContrasena) {
  let esValido = true;
  const expresionCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!correo) {
    errorCorreo.textContent = "El correo es obligatorio.";
    esValido = false;
  } else if (!expresionCorreo.test(correo)) {
    errorCorreo.textContent = "Ingresa un correo válido.";
    esValido = false;
  }

  if (!contrasena) {
    errorContrasena.textContent = "La contraseña es obligatoria.";
    esValido = false;
  }

  return esValido;
}


function alternarVisibilidadContrasena(campo, icono) {
  const esVisible = campo.type === "text";
  campo.type = esVisible ? "password" : "text";
  icono.classList.toggle("bi-eye", esVisible);
  icono.classList.toggle("bi-eye-slash", !esVisible);
}

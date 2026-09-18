package com.accesorios.gestion.service

import com.accesorios.gestion.dto.autenticacion.LoginRequest
import com.accesorios.gestion.dto.autenticacion.RegistroRequest
import com.accesorios.gestion.dto.autenticacion.TokenResponse
import com.accesorios.gestion.dto.autenticacion.UsuarioResponse
import com.accesorios.gestion.exception.CredencialesInvalidasException
import com.accesorios.gestion.exception.OperacionNoPermitidaException
import com.accesorios.gestion.exception.RecursoDuplicadoException
import com.accesorios.gestion.model.AccionAuditoria
import com.accesorios.gestion.model.RolUsuario
import com.accesorios.gestion.model.Usuario
import com.accesorios.gestion.repository.UsuarioRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class ServicioAutenticacion(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    private val servicioJwt: ServicioJwt,
    private val authenticationManager: AuthenticationManager,
    private val servicioAuditoria: ServicioAuditoria
) {


    fun registrar(request: RegistroRequest, correoCreador: String): UsuarioResponse {
        val creador = buscarUsuarioPorCorreo(correoCreador)
        validarPermisoDeCreacion(creador.rol, request.rol)

        val correo = request.correo.trim().lowercase()
        if (usuarioRepository.existsByCorreo(correo)) {
            throw RecursoDuplicadoException("El correo ya se encuentra registrado")
        }

        val usuario = Usuario(
            correo = correo,
            nombreCompleto = request.nombreCompleto.trim(),
            contrasena = passwordEncoder.encode(request.contrasena),
            rol = request.rol
        )

        val guardado = usuarioRepository.save(usuario)
        servicioAuditoria.registrar(correoCreador, AccionAuditoria.CREACION, "Usuario", requireNotNull(guardado.id))
        return guardado.toResponse()
    }

    fun login(request: LoginRequest): TokenResponse {
        val correo = request.correo.trim().lowercase()

        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(correo, request.contrasena)
            )
        } catch (ex: DisabledException) {
            throw CredencialesInvalidasException("Credenciales inválidas")
        } catch (ex: BadCredentialsException) {
            throw CredencialesInvalidasException("Credenciales inválidas")
        }

        val usuario = usuarioRepository.findByCorreo(correo)
            ?: throw CredencialesInvalidasException("Credenciales inválidas")

        val token = servicioJwt.generarToken(usuario)

        return TokenResponse(
            tokenAcceso = token,
            expiraEnSegundos = servicioJwt.minutosExpiracion * 60
        )
    }

    private fun validarPermisoDeCreacion(rolCreador: RolUsuario, rolObjetivo: RolUsuario) {
        val permitido = when (rolCreador) {
            RolUsuario.SUPER_USUARIO -> rolObjetivo == RolUsuario.ADMINISTRADOR || rolObjetivo == RolUsuario.EMPLEADO
            RolUsuario.ADMINISTRADOR -> rolObjetivo == RolUsuario.EMPLEADO
            RolUsuario.EMPLEADO -> false
        }

        if (!permitido) {
            throw OperacionNoPermitidaException(
                "El rol ${rolCreador.name} no tiene permiso para crear usuarios con rol ${rolObjetivo.name}"
            )
        }
    }

    private fun buscarUsuarioPorCorreo(correo: String) =
        usuarioRepository.findByCorreo(correo)
            ?: throw CredencialesInvalidasException("Usuario no encontrado")

    private fun Usuario.toResponse(): UsuarioResponse {
        return UsuarioResponse(
            id = requireNotNull(id),
            correo = correo,
            nombreCompleto = nombreCompleto,
            rol = rol,
            activo = activo,
            fechaCreacion = fechaCreacion
        )
    }
}

package com.accesorios.gestion.config

import com.accesorios.gestion.model.RolUsuario
import com.accesorios.gestion.model.Usuario
import com.accesorios.gestion.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component


@Component
class InicializadorDatos(
    private val usuarioRepository: UsuarioRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${app.seed.super-user.email}") private val correoSemilla: String,
    @param:Value("\${app.seed.super-user.password}") private val contrasenaSemilla: String
) : CommandLineRunner {

    override fun run(vararg args: String) {
        if (!usuarioRepository.existsByCorreo(correoSemilla)) {
            val superUsuario = Usuario(
                correo = correoSemilla,
                nombreCompleto = "Super Usuario",
                contrasena = passwordEncoder.encode(contrasenaSemilla),
                rol = RolUsuario.SUPER_USUARIO
            )
            usuarioRepository.save(superUsuario)
        }
    }
}

package com.accesorios.gestion.config

import com.accesorios.gestion.repository.UsuarioRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class ServicioDetallesUsuarioPersonalizado(
    private val usuarioRepository: UsuarioRepository
) : UserDetailsService {

    override fun loadUserByUsername(correo: String): UserDetails {
        val usuario = usuarioRepository.findByCorreo(correo)
            ?: throw UsernameNotFoundException("Usuario no encontrado: $correo")

        val autoridades = usuario.rol.rolesImplicitos().map { SimpleGrantedAuthority("ROLE_${it.name}") }

        return org.springframework.security.core.userdetails.User.builder()
            .username(usuario.correo)
            .password(usuario.contrasena)
            .disabled(!usuario.activo)
            .authorities(autoridades)
            .build()
    }
}

package com.accesorios.gestion.config

import com.accesorios.gestion.repository.UsuarioRepository
import com.accesorios.gestion.service.ServicioJwt
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FiltroAutenticacionJwt(
    private val servicioJwt: ServicioJwt,
    private val usuarioRepository: UsuarioRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.removePrefix("Bearer ").trim()
        val correo = servicioJwt.extraerCorreo(token)

        if (correo != null && servicioJwt.esTokenValido(token) && SecurityContextHolder.getContext().authentication == null) {
            val usuario = usuarioRepository.findByCorreo(correo)
            if (usuario != null && usuario.activo) {
                val autoridades = usuario.rol.rolesImplicitos().map { SimpleGrantedAuthority("ROLE_${it.name}") }
                val authentication = UsernamePasswordAuthenticationToken(
                    usuario.correo,
                    null,
                    autoridades
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}

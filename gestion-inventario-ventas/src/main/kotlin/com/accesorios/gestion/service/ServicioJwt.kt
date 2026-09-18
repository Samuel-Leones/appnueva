package com.accesorios.gestion.service

import com.accesorios.gestion.model.Usuario
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class ServicioJwt(
    @param:Value("\${app.jwt.secret}") private val secreto: String,
    @param:Value("\${app.jwt.expiration-minutes}") val minutosExpiracion: Long
) {

    private val claveFirma: SecretKey by lazy {
        Keys.hmacShaKeyFor(secreto.toByteArray(Charsets.UTF_8))
    }

    fun generarToken(usuario: Usuario): String {
        val ahora = Date()
        val expiracion = Date(ahora.time + minutosExpiracion * 60_000)

        return Jwts.builder()
            .subject(usuario.correo)
            .claim("rol", usuario.rol.name)
            .claim("usuarioId", usuario.id)
            .issuedAt(ahora)
            .expiration(expiracion)
            .signWith(claveFirma)
            .compact()
    }

    fun extraerCorreo(token: String): String? {
        return extraerTodosLosClaims(token)?.subject
    }

    fun esTokenValido(token: String): Boolean {
        return try {
            extraerTodosLosClaims(token) != null && !estaTokenExpirado(token)
        } catch (ex: Exception) {
            false
        }
    }

    private fun estaTokenExpirado(token: String): Boolean {
        val expiracion = extraerTodosLosClaims(token)?.expiration ?: return true
        return expiracion.before(Date())
    }

    private fun extraerTodosLosClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(claveFirma)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (ex: Exception) {
            null
        }
    }
}

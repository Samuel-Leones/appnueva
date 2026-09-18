package com.accesorios.gestion.repository

import com.accesorios.gestion.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByCorreo(correo: String): Usuario?
    fun existsByCorreo(correo: String): Boolean
}

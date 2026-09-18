package com.accesorios.gestion.dto.autenticacion

import com.accesorios.gestion.model.RolUsuario
import java.time.LocalDateTime

data class UsuarioResponse(
    val id: Long,
    val correo: String,
    val nombreCompleto: String,
    val rol: RolUsuario,
    val activo: Boolean,
    val fechaCreacion: LocalDateTime
)

package com.accesorios.gestion.dto.auditoria

import com.accesorios.gestion.model.AccionAuditoria
import java.time.LocalDateTime

data class RegistroAuditoriaResponse(
    val id: Long,
    val usuarioId: Long,
    val correoUsuario: String,
    val accion: AccionAuditoria,
    val entidadAfectada: String,
    val idEntidadAfectada: Long,
    val fechaHora: LocalDateTime
)

package com.accesorios.gestion.dto.cliente

import java.time.LocalDateTime

data class ClienteResponse(
    val id: Long,
    val nombre: String,
    val documento: String,
    val telefono: String?,
    val correo: String?,
    val direccion: String?,
    val fechaRegistro: LocalDateTime
)

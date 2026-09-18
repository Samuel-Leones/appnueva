package com.accesorios.gestion.dto.autenticacion

data class TokenResponse(
    val tokenAcceso: String,
    val tipoToken: String = "Bearer",
    val expiraEnSegundos: Long
)

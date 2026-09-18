package com.accesorios.gestion.dto.cliente

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ClienteRequest(
    @field:NotBlank
    val nombre: String,

    @field:NotBlank
    val documento: String,

    val telefono: String?,

    @field:Email
    val correo: String?,

    val direccion: String?
)

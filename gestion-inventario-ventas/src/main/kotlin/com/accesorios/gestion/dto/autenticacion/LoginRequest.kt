package com.accesorios.gestion.dto.autenticacion

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email
    @field:NotBlank
    val correo: String,

    @field:NotBlank
    val contrasena: String
)

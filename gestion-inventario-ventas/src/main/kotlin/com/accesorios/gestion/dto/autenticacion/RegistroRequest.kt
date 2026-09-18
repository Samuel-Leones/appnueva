package com.accesorios.gestion.dto.autenticacion

import com.accesorios.gestion.model.RolUsuario
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegistroRequest(
    @field:Email
    @field:NotBlank
    val correo: String,

    @field:NotBlank
    val nombreCompleto: String,

    @field:NotBlank
    @field:Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    val contrasena: String,

    @field:NotNull
    val rol: RolUsuario
)

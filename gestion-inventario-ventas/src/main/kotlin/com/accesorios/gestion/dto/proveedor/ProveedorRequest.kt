package com.accesorios.gestion.dto.proveedor

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ProveedorRequest(
    @field:NotBlank
    val nombre: String,

    val nombreContacto: String?,

    val telefono: String?,

    @field:Email
    val correo: String?
)

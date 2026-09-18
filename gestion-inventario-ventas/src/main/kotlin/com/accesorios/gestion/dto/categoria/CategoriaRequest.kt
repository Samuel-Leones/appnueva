package com.accesorios.gestion.dto.categoria

import jakarta.validation.constraints.NotBlank

data class CategoriaRequest(
    @field:NotBlank
    val nombre: String,

    val descripcion: String?
)

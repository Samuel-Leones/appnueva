package com.accesorios.gestion.dto.producto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class ProductoRequest(
    @field:NotBlank
    val nombre: String,

    val descripcion: String?,

    @field:NotBlank
    val codigo: String,

    @field:NotNull
    val categoriaId: Long,

    val proveedorId: Long?,

    @field:NotNull
    @field:Positive(message = "El precio debe ser mayor a 0")
    val precio: BigDecimal,

    @field:PositiveOrZero
    val stockInicial: Int = 0,

    @field:PositiveOrZero
    val stockMinimo: Int = 0
)

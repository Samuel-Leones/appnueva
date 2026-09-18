package com.accesorios.gestion.dto.venta

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class ItemVentaRequest(
    @field:NotNull
    val productoId: Long,

    @field:Positive
    val cantidad: Int
)

package com.accesorios.gestion.dto.venta

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class VentaRequest(
    @field:NotNull
    val clienteId: Long,

    @field:NotEmpty(message = "La venta debe incluir al menos un producto")
    @field:Valid
    val items: List<ItemVentaRequest>
)

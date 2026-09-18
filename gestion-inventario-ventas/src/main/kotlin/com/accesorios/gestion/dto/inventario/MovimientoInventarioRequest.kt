package com.accesorios.gestion.dto.inventario

import com.accesorios.gestion.model.TipoMovimientoInventario
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class MovimientoInventarioRequest(
    @field:NotNull
    val productoId: Long,

    @field:NotNull
    val tipo: TipoMovimientoInventario,

    @field:Positive
    val cantidad: Int,

    val motivo: String?
)

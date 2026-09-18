package com.accesorios.gestion.dto.inventario

import com.accesorios.gestion.model.TipoMovimientoInventario
import java.time.LocalDateTime

data class MovimientoInventarioResponse(
    val id: Long,
    val productoId: Long,
    val nombreProducto: String,
    val tipo: TipoMovimientoInventario,
    val cantidad: Int,
    val motivo: String?,
    val stockResultante: Int,
    val correoUsuario: String,
    val fecha: LocalDateTime
)

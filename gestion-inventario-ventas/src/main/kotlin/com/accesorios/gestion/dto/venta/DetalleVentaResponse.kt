package com.accesorios.gestion.dto.venta

import java.math.BigDecimal

data class DetalleVentaResponse(
    val productoId: Long,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: BigDecimal,
    val totalLinea: BigDecimal
)

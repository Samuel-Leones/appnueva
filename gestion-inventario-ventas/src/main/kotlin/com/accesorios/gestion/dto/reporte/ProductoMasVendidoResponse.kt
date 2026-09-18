package com.accesorios.gestion.dto.reporte

data class ProductoMasVendidoResponse(
    val productoId: Long,
    val nombreProducto: String,
    val cantidadTotalVendida: Long
)

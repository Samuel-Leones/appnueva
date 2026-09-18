package com.accesorios.gestion.dto.reporte

data class ReporteInventarioItemResponse(
    val productoId: Long,
    val nombreProducto: String,
    val codigo: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val stockBajo: Boolean
)

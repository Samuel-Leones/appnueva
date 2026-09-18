package com.accesorios.gestion.dto.reporte

import java.math.BigDecimal

data class ReporteVentasResponse(
    val etiquetaPeriodo: String,
    val totalVentas: BigDecimal,
    val cantidadTransacciones: Long
)

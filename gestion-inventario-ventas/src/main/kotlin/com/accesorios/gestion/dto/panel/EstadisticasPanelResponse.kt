package com.accesorios.gestion.dto.panel

import java.math.BigDecimal

data class EstadisticasPanelResponse(
    val totalVentasDelMes: BigDecimal,
    val cantidadProductosActivos: Long,
    val cantidadProductosAgotados: Long,
    val cantidadClientesRegistrados: Long
)

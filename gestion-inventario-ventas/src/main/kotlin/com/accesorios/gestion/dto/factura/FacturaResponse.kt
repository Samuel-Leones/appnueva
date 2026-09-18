package com.accesorios.gestion.dto.factura

import com.accesorios.gestion.dto.venta.VentaResponse
import java.time.LocalDateTime

data class FacturaResponse(
    val id: Long,
    val numeroFactura: String,
    val fechaEmision: LocalDateTime,
    val venta: VentaResponse
)

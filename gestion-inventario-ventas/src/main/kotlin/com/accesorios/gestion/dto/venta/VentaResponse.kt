package com.accesorios.gestion.dto.venta

import com.accesorios.gestion.model.EstadoVenta
import java.math.BigDecimal
import java.time.LocalDateTime

data class VentaResponse(
    val id: Long,
    val clienteId: Long,
    val nombreCliente: String,
    val vendedorId: Long,
    val correoVendedor: String,
    val subtotal: BigDecimal,
    val iva: BigDecimal,
    val total: BigDecimal,
    val estado: EstadoVenta,
    val detalles: List<DetalleVentaResponse>,
    val fechaCreacion: LocalDateTime
)

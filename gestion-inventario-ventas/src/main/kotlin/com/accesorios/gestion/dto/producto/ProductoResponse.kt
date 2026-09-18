package com.accesorios.gestion.dto.producto

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val codigo: String,
    val categoriaId: Long,
    val nombreCategoria: String,
    val proveedorId: Long?,
    val nombreProveedor: String?,
    val precio: BigDecimal,
    val stockActual: Int,
    val stockMinimo: Int,
    val activo: Boolean,
    val stockBajo: Boolean,
    val mensajeAlerta: String?,
    val fechaCreacion: LocalDateTime,
    val fechaActualizacion: LocalDateTime
)
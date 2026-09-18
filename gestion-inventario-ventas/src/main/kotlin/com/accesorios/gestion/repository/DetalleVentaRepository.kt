package com.accesorios.gestion.repository

import com.accesorios.gestion.model.DetalleVenta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface DetalleVentaRepository : JpaRepository<DetalleVenta, Long> {

    @Query(
        """
        SELECT d.producto.id AS productoId, d.producto.nombre AS nombreProducto, SUM(d.cantidad) AS cantidadTotal
        FROM DetalleVenta d
        WHERE d.venta.estado = com.accesorios.gestion.model.EstadoVenta.COMPLETADA
        AND d.venta.fechaCreacion BETWEEN :from AND :to
        GROUP BY d.producto.id, d.producto.nombre
        ORDER BY SUM(d.cantidad) DESC
        """
    )
    fun buscarProductosMasVendidos(
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): List<ProductoMasVendidoProjection>
}

interface ProductoMasVendidoProjection {
    fun getProductoId(): Long
    fun getNombreProducto(): String
    fun getCantidadTotal(): Long
}

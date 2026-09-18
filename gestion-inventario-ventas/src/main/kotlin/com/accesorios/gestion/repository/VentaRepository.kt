package com.accesorios.gestion.repository

import com.accesorios.gestion.model.EstadoVenta
import com.accesorios.gestion.model.Venta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDateTime

interface VentaRepository : JpaRepository<Venta, Long> {

    fun findAllByClienteIdOrderByFechaCreacionDesc(clienteId: Long): List<Venta>

    fun existsByClienteId(clienteId: Long): Boolean

    fun findAllByEstadoAndFechaCreacionBetween(
        estado: EstadoVenta,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Venta>

    fun countByEstadoAndFechaCreacionBetween(
        estado: EstadoVenta,
        from: LocalDateTime,
        to: LocalDateTime
    ): Long

    @Query(
        "SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = :estado AND v.fechaCreacion BETWEEN :from AND :to"
    )
    fun sumarTotalPorEstadoYFecha(
        @Param("estado") estado: EstadoVenta,
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime
    ): BigDecimal
}
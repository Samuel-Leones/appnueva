package com.accesorios.gestion.repository

import com.accesorios.gestion.model.MovimientoInventario
import org.springframework.data.jpa.repository.JpaRepository

interface MovimientoInventarioRepository : JpaRepository<MovimientoInventario, Long> {
    fun findAllByProductoIdOrderByFechaDesc(productoId: Long): List<MovimientoInventario>
}

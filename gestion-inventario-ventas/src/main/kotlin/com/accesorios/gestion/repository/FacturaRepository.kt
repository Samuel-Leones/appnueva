package com.accesorios.gestion.repository

import com.accesorios.gestion.model.Factura
import org.springframework.data.jpa.repository.JpaRepository

interface FacturaRepository : JpaRepository<Factura, Long> {
    fun findByVentaId(ventaId: Long): Factura?
    fun existsByNumeroFactura(numeroFactura: String): Boolean
}

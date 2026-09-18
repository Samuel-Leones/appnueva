package com.accesorios.gestion.service

import com.accesorios.gestion.dto.factura.FacturaResponse
import com.accesorios.gestion.exception.RecursoNoEncontradoException
import com.accesorios.gestion.model.Factura
import com.accesorios.gestion.model.Venta
import com.accesorios.gestion.repository.FacturaRepository
import com.accesorios.gestion.util.GeneradorNumeroFactura
import org.springframework.stereotype.Service

@Service
class ServicioFactura(
    private val facturaRepository: FacturaRepository
) {


    fun generarParaVenta(venta: Venta): Factura {
        var secuencia = facturaRepository.count() + 1
        var numeroFactura = GeneradorNumeroFactura.generar(secuencia)

        while (facturaRepository.existsByNumeroFactura(numeroFactura)) {
            secuencia += 1
            numeroFactura = GeneradorNumeroFactura.generar(secuencia)
        }

        val factura = Factura(venta = venta, numeroFactura = numeroFactura)
        return facturaRepository.save(factura)
    }

    fun obtenerPorVentaId(ventaId: Long): FacturaResponse {
        val factura = facturaRepository.findByVentaId(ventaId)
            ?: throw RecursoNoEncontradoException("La venta aún no tiene una factura generada")
        return factura.toResponse()
    }

    private fun Factura.toResponse(): FacturaResponse = FacturaResponse(
        id = requireNotNull(id),
        numeroFactura = numeroFactura,
        fechaEmision = fechaEmision,
        venta = venta.toVentaResponse()
    )
}

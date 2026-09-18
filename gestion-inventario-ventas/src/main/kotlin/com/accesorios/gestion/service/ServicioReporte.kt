package com.accesorios.gestion.service

import com.accesorios.gestion.dto.reporte.ProductoMasVendidoResponse
import com.accesorios.gestion.dto.reporte.ReporteInventarioItemResponse
import com.accesorios.gestion.dto.reporte.ReporteVentasResponse
import com.accesorios.gestion.model.EstadoVenta
import com.accesorios.gestion.repository.DetalleVentaRepository
import com.accesorios.gestion.repository.ProductoRepository
import com.accesorios.gestion.repository.VentaRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class ServicioReporte(
    private val ventaRepository: VentaRepository,
    private val detalleVentaRepository: DetalleVentaRepository,
    private val productoRepository: ProductoRepository
) {

    fun ventasDiarias(fecha: LocalDate): ReporteVentasResponse {
        val desde = fecha.atStartOfDay()
        val hasta = fecha.plusDays(1).atStartOfDay()

        return ReporteVentasResponse(
            etiquetaPeriodo = fecha.toString(),
            totalVentas = ventaRepository.sumarTotalPorEstadoYFecha(EstadoVenta.COMPLETADA, desde, hasta),
            cantidadTransacciones = ventaRepository.countByEstadoAndFechaCreacionBetween(EstadoVenta.COMPLETADA, desde, hasta)
        )
    }

    fun ventasMensuales(mesAno: YearMonth): ReporteVentasResponse {
        val desde = mesAno.atDay(1).atStartOfDay()
        val hasta = mesAno.plusMonths(1).atDay(1).atStartOfDay()

        return ReporteVentasResponse(
            etiquetaPeriodo = mesAno.toString(),
            totalVentas = ventaRepository.sumarTotalPorEstadoYFecha(EstadoVenta.COMPLETADA, desde, hasta),
            cantidadTransacciones = ventaRepository.countByEstadoAndFechaCreacionBetween(EstadoVenta.COMPLETADA, desde, hasta)
        )
    }

    fun productosMasVendidos(desde: LocalDate, hasta: LocalDate): List<ProductoMasVendidoResponse> {
        val desdeFechaHora = desde.atStartOfDay()
        val hastaFechaHora = hasta.plusDays(1).atStartOfDay()

        return detalleVentaRepository.buscarProductosMasVendidos(desdeFechaHora, hastaFechaHora).map {
            ProductoMasVendidoResponse(
                productoId = it.getProductoId(),
                nombreProducto = it.getNombreProducto(),
                cantidadTotalVendida = it.getCantidadTotal()
            )
        }
    }

    fun reporteInventario(): List<ReporteInventarioItemResponse> =
        productoRepository.findAllByActivoTrue().map {
            ReporteInventarioItemResponse(
                productoId = requireNotNull(it.id),
                nombreProducto = it.nombre,
                codigo = it.codigo,
                stockActual = it.stockActual,
                stockMinimo = it.stockMinimo,
                stockBajo = it.stockActual <= it.stockMinimo
            )
        }
}

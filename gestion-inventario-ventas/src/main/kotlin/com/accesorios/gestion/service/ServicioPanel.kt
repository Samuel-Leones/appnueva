package com.accesorios.gestion.service

import com.accesorios.gestion.dto.panel.EstadisticasPanelResponse
import com.accesorios.gestion.model.EstadoVenta
import com.accesorios.gestion.repository.ClienteRepository
import com.accesorios.gestion.repository.ProductoRepository
import com.accesorios.gestion.repository.VentaRepository
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class ServicioPanel(
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository,
    private val clienteRepository: ClienteRepository
) {

    fun obtenerEstadisticas(): EstadisticasPanelResponse {
        val mesActual = YearMonth.now()
        val desde = mesActual.atDay(1).atStartOfDay()
        val hasta = mesActual.plusMonths(1).atDay(1).atStartOfDay()

        return EstadisticasPanelResponse(
            totalVentasDelMes = ventaRepository.sumarTotalPorEstadoYFecha(EstadoVenta.COMPLETADA, desde, hasta),
            cantidadProductosActivos = productoRepository.countByActivoTrue(),
            cantidadProductosAgotados = productoRepository.countByActivoTrueAndStockActual(0),
            cantidadClientesRegistrados = clienteRepository.count()
        )
    }
}

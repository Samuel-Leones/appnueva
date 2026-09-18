package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.reporte.ProductoMasVendidoResponse
import com.accesorios.gestion.dto.reporte.ReporteInventarioItemResponse
import com.accesorios.gestion.dto.reporte.ReporteVentasResponse
import com.accesorios.gestion.service.ServicioReporte
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.YearMonth

@RestController
@RequestMapping("/reportes")
class ControladorReporte(
    private val servicioReporte: ServicioReporte
) {

    @GetMapping("/ventas-diarias")
    fun ventasDiarias(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fecha: LocalDate
    ): ReporteVentasResponse = servicioReporte.ventasDiarias(fecha)

    @GetMapping("/ventas-mensuales")
    fun ventasMensuales(@RequestParam anio: Int, @RequestParam mes: Int): ReporteVentasResponse =
        servicioReporte.ventasMensuales(YearMonth.of(anio, mes))

    @GetMapping("/productos-mas-vendidos")
    fun productosMasVendidos(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) desde: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) hasta: LocalDate
    ): List<ProductoMasVendidoResponse> = servicioReporte.productosMasVendidos(desde, hasta)

    @GetMapping("/inventario")
    fun inventario(): List<ReporteInventarioItemResponse> = servicioReporte.reporteInventario()
}

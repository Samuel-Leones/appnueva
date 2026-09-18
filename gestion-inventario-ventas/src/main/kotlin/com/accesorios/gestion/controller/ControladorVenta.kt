package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.factura.FacturaResponse
import com.accesorios.gestion.dto.venta.VentaRequest
import com.accesorios.gestion.dto.venta.VentaResponse
import com.accesorios.gestion.service.ServicioFactura
import com.accesorios.gestion.service.ServicioVenta
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ventas")
class ControladorVenta(
    private val servicioVenta: ServicioVenta,
    private val servicioFactura: ServicioFactura
) {

    @PostMapping
    fun crear(
        @Valid @RequestBody request: VentaRequest,
        authentication: Authentication
    ): ResponseEntity<VentaResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(servicioVenta.crear(request, authentication.name))
    }

    @GetMapping("/{id}")
    fun obtener(@PathVariable id: Long): VentaResponse = servicioVenta.obtenerPorId(id)

    @PostMapping("/{id}/completar")
    fun completar(@PathVariable id: Long, authentication: Authentication): VentaResponse =
        servicioVenta.completar(id, authentication.name)

    @PostMapping("/{id}/anular")
    fun anular(@PathVariable id: Long, authentication: Authentication): VentaResponse =
        servicioVenta.anular(id, authentication.name)

    @GetMapping("/{id}/factura")
    fun factura(@PathVariable id: Long): FacturaResponse = servicioFactura.obtenerPorVentaId(id)
}

package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.inventario.MovimientoInventarioRequest
import com.accesorios.gestion.dto.inventario.MovimientoInventarioResponse
import com.accesorios.gestion.service.ServicioInventario
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
@RequestMapping("/inventario")
class ControladorInventario(
    private val servicioInventario: ServicioInventario
) {

    @PostMapping("/movimientos")
    fun registrar(
        @Valid @RequestBody request: MovimientoInventarioRequest,
        authentication: Authentication
    ): ResponseEntity<MovimientoInventarioResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(servicioInventario.registrarMovimiento(request, authentication.name))
    }

    @GetMapping("/movimientos/producto/{productoId}")
    fun historial(@PathVariable productoId: Long): List<MovimientoInventarioResponse> =
        servicioInventario.obtenerMovimientosPorProducto(productoId)
}

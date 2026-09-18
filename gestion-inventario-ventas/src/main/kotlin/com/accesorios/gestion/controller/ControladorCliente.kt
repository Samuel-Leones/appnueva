package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.cliente.ClienteRequest
import com.accesorios.gestion.dto.cliente.ClienteResponse
import com.accesorios.gestion.dto.cliente.ClienteUpdateRequest
import com.accesorios.gestion.dto.venta.VentaResponse
import com.accesorios.gestion.service.ServicioCliente
import com.accesorios.gestion.service.ServicioVenta
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/clientes")
class ControladorCliente(
    private val servicioCliente: ServicioCliente,
    private val servicioVenta: ServicioVenta
) {

    @GetMapping
    fun listar(
        @RequestParam(required = false)
        buscar: String?
    ): List<ClienteResponse> {

        return if (buscar.isNullOrBlank())
            servicioCliente.obtenerTodos()
        else
            servicioCliente.buscar(buscar)
    }

    @PostMapping
    fun crear(
        @Valid @RequestBody request: ClienteRequest,
        authentication: Authentication
    ): ResponseEntity<ClienteResponse> {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(servicioCliente.crear(request, authentication.name))
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @RequestBody request: ClienteUpdateRequest,
        authentication: Authentication
    ): ClienteResponse =
        servicioCliente.actualizar(id, request, authentication.name)

    @DeleteMapping("/{id}")
    fun eliminar(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {

        servicioCliente.eliminar(id, authentication.name)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/compras")
    fun historialCompras(
        @PathVariable id: Long
    ): List<VentaResponse> =
        servicioVenta.obtenerHistorialCompras(id)
}
package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.proveedor.ProveedorRequest
import com.accesorios.gestion.dto.proveedor.ProveedorResponse
import com.accesorios.gestion.service.ServicioProveedor
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/proveedores")
class ControladorProveedor(
    private val servicioProveedor: ServicioProveedor
) {

    @GetMapping
    fun listar(): List<ProveedorResponse> = servicioProveedor.obtenerTodos()

    @PostMapping
    fun crear(
        @Valid @RequestBody request: ProveedorRequest,
        authentication: Authentication
    ): ResponseEntity<ProveedorResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(servicioProveedor.crear(request, authentication.name))
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProveedorRequest,
        authentication: Authentication
    ): ProveedorResponse = servicioProveedor.actualizar(id, request, authentication.name)

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long, authentication: Authentication): ResponseEntity<Void> {
        servicioProveedor.eliminar(id, authentication.name)
        return ResponseEntity.noContent().build()
    }
}

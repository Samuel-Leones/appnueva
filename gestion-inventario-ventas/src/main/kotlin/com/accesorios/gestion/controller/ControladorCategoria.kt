package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.categoria.CategoriaRequest
import com.accesorios.gestion.dto.categoria.CategoriaResponse
import com.accesorios.gestion.dto.categoria.CategoriaUpdateRequest
import com.accesorios.gestion.service.ServicioCategoria
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categorias")
class ControladorCategoria(
    private val servicioCategoria: ServicioCategoria
) {

    @GetMapping
    fun listar(): List<CategoriaResponse> =
        servicioCategoria.obtenerTodas()

    @PostMapping
    fun crear(
        @Valid @RequestBody request: CategoriaRequest,
        authentication: Authentication
    ): ResponseEntity<CategoriaResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(servicioCategoria.crear(request, authentication.name))
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @RequestBody request: CategoriaUpdateRequest,
        authentication: Authentication
    ): CategoriaResponse {
        return servicioCategoria.actualizar(id, request, authentication.name)
    }

    @DeleteMapping("/{id}")
    fun eliminar(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        servicioCategoria.eliminar(id, authentication.name)
        return ResponseEntity.noContent().build()
    }
}
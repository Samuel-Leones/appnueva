package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.producto.ProductoRequest
import com.accesorios.gestion.dto.producto.ProductoResponse
import com.accesorios.gestion.service.ServicioProducto
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/productos")
class ControladorProducto(
    private val servicioProducto: ServicioProducto
) {

    @GetMapping
    fun listar(
        @RequestParam(required = false) nombre: String?,
        @RequestParam(required = false) categoriaId: Long?,
        @RequestParam(required = false) codigo: String?
    ): List<ProductoResponse> {
        val esBusqueda = nombre != null || categoriaId != null || codigo != null
        return if (esBusqueda) servicioProducto.buscar(nombre, categoriaId, codigo) else servicioProducto.obtenerTodos()
    }

    @GetMapping("/codigo/{codigo}")
    fun obtenerPorCodigo(@PathVariable codigo: String): ProductoResponse = servicioProducto.obtenerPorCodigo(codigo)

    @GetMapping("/bajo-stock")
    fun bajoStock(): List<ProductoResponse> = servicioProducto.obtenerBajoStock()

    @PostMapping
    fun crear(
        @Valid @RequestBody request: ProductoRequest,
        authentication: Authentication
    ): ResponseEntity<ProductoResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(servicioProducto.crear(request, authentication.name))
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductoRequest,
        authentication: Authentication
    ): ProductoResponse = servicioProducto.actualizar(id, request, authentication.name)

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long, authentication: Authentication): ResponseEntity<Void> {
        servicioProducto.eliminar(id, authentication.name)
        return ResponseEntity.noContent().build()
    }
}

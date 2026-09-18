package com.accesorios.gestion.service

import com.accesorios.gestion.dto.producto.ProductoRequest
import com.accesorios.gestion.dto.producto.ProductoResponse
import com.accesorios.gestion.exception.RecursoDuplicadoException
import com.accesorios.gestion.exception.RecursoNoEncontradoException
import com.accesorios.gestion.model.AccionAuditoria
import com.accesorios.gestion.model.Producto
import com.accesorios.gestion.repository.ProductoRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ServicioProducto(
    private val productoRepository: ProductoRepository,
    private val servicioCategoria: ServicioCategoria,
    private val servicioProveedor: ServicioProveedor,
    private val servicioAuditoria: ServicioAuditoria
) {

    fun crear(request: ProductoRequest, responsable: String): ProductoResponse {
        if (productoRepository.existsByCodigo(request.codigo.trim())) {
            throw RecursoDuplicadoException("Ya existe un producto con ese código")
        }

        val categoria = servicioCategoria.obtenerPorIdOLanzar(request.categoriaId)
        val proveedor = request.proveedorId?.let { servicioProveedor.obtenerPorIdOLanzar(it) }

        val producto = Producto(
            nombre = request.nombre.trim(),
            descripcion = request.descripcion?.trim(),
            codigo = request.codigo.trim(),
            categoria = categoria,
            proveedor = proveedor,
            precio = request.precio,
            stockActual = request.stockInicial,
            stockMinimo = request.stockMinimo
        )

        val guardado = productoRepository.save(producto)
        servicioAuditoria.registrar(responsable, AccionAuditoria.CREACION, "Producto", requireNotNull(guardado.id))
        return guardado.toResponse()
    }

    fun actualizar(id: Long, request: ProductoRequest, responsable: String): ProductoResponse {
        val producto = buscarActivoOLanzar(id)

        if (productoRepository.existsByCodigoAndIdNot(request.codigo.trim(), id)) {
            throw RecursoDuplicadoException("Ya existe un producto con ese código")
        }

        producto.nombre = request.nombre.trim()
        producto.descripcion = request.descripcion?.trim()
        producto.codigo = request.codigo.trim()
        producto.categoria = servicioCategoria.obtenerPorIdOLanzar(request.categoriaId)
        producto.proveedor = request.proveedorId?.let { servicioProveedor.obtenerPorIdOLanzar(it) }
        producto.precio = request.precio
        producto.stockMinimo = request.stockMinimo
        producto.fechaActualizacion = LocalDateTime.now()

        val guardado = productoRepository.save(producto)
        servicioAuditoria.registrar(responsable, AccionAuditoria.ACTUALIZACION, "Producto", id)
        return guardado.toResponse()
    }


    fun eliminar(id: Long, responsable: String) {
        val producto = buscarActivoOLanzar(id)
        producto.activo = false
        producto.fechaActualizacion = LocalDateTime.now()
        productoRepository.save(producto)
        servicioAuditoria.registrar(responsable, AccionAuditoria.ELIMINACION, "Producto", id)
    }

    fun obtenerTodos(): List<ProductoResponse> =
        productoRepository.findAllByActivoTrue().map { it.toResponse() }

    fun obtenerPorCodigo(codigo: String): ProductoResponse =
        (productoRepository.findByCodigoAndActivoTrue(codigo)
            ?: throw RecursoNoEncontradoException("Producto no encontrado"))
            .toResponse()

    fun buscar(nombre: String?, categoriaId: Long?, codigo: String?): List<ProductoResponse> =
        productoRepository.buscar(
            nombre?.trim()?.ifBlank { null },
            categoriaId,
            codigo?.trim()?.ifBlank { null }
        ).map { it.toResponse() }

    fun obtenerBajoStock(): List<ProductoResponse> =
        productoRepository.findAllBajoStock().map { it.toResponse() }


    fun obtenerEntidadActivaOLanzar(id: Long): Producto = buscarActivoOLanzar(id)

    fun guardar(producto: Producto): Producto = productoRepository.save(producto)

    private fun buscarActivoOLanzar(id: Long): Producto =
        productoRepository.findById(id)
            .filter { it.activo }
            .orElseThrow { RecursoNoEncontradoException("Producto no encontrado") }

    private fun Producto.toResponse() = ProductoResponse(
        id = requireNotNull(id),
        nombre = nombre,
        descripcion = descripcion,
        codigo = codigo,
        categoriaId = requireNotNull(categoria.id),
        nombreCategoria = categoria.nombre,
        proveedorId = proveedor?.id,
        nombreProveedor = proveedor?.nombre,
        precio = precio,
        stockActual = stockActual,
        stockMinimo = stockMinimo,
        activo = activo,
        stockBajo = stockActual <= stockMinimo,
        mensajeAlerta = generarMensajeAlerta(stockActual, stockMinimo, nombre),
        fechaCreacion = fechaCreacion,
        fechaActualizacion = fechaActualizacion
    )

    private fun generarMensajeAlerta(stockActual: Int, stockMinimo: Int, nombreProducto: String): String? {
        if (stockActual > stockMinimo) return null

        return if (stockActual == 0) {
            "Producto agotado: '$nombreProducto' no tiene unidades disponibles."
        } else {
            "Alerta de stock bajo: '$nombreProducto' tiene $stockActual unidad(es), por debajo del mínimo configurado ($stockMinimo)."
        }
    }
}
package com.accesorios.gestion.service

import com.accesorios.gestion.dto.inventario.MovimientoInventarioRequest
import com.accesorios.gestion.dto.inventario.MovimientoInventarioResponse
import com.accesorios.gestion.exception.CredencialesInvalidasException
import com.accesorios.gestion.exception.StockInsuficienteException
import com.accesorios.gestion.model.AccionAuditoria
import com.accesorios.gestion.model.MovimientoInventario
import com.accesorios.gestion.model.Producto
import com.accesorios.gestion.model.TipoMovimientoInventario
import com.accesorios.gestion.repository.MovimientoInventarioRepository
import com.accesorios.gestion.repository.UsuarioRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ServicioInventario(
    private val movimientoInventarioRepository: MovimientoInventarioRepository,
    private val servicioProducto: ServicioProducto,
    private val usuarioRepository: UsuarioRepository,
    private val servicioAuditoria: ServicioAuditoria
) {

    fun registrarMovimiento(request: MovimientoInventarioRequest, responsable: String): MovimientoInventarioResponse {
        val producto = servicioProducto.obtenerEntidadActivaOLanzar(request.productoId)
        val movimiento = aplicarCambioStock(producto, request.cantidad, request.tipo, request.motivo, responsable)
        return movimiento.toResponse()
    }


    fun aplicarCambioStock(
        producto: Producto,
        cantidad: Int,
        tipo: TipoMovimientoInventario,
        motivo: String?,
        responsable: String
    ): MovimientoInventario {
        val nuevoStock = when (tipo) {
            TipoMovimientoInventario.ENTRADA -> producto.stockActual + cantidad
            TipoMovimientoInventario.SALIDA -> producto.stockActual - cantidad
        }

        if (nuevoStock < 0) {
            throw StockInsuficienteException(
                "Stock insuficiente para el producto '${producto.nombre}'. Disponible: ${producto.stockActual}, solicitado: $cantidad"
            )
        }

        producto.stockActual = nuevoStock
        producto.fechaActualizacion = LocalDateTime.now()
        servicioProducto.guardar(producto)

        val usuario = usuarioRepository.findByCorreo(responsable)
            ?: throw CredencialesInvalidasException("Usuario no encontrado")

        val movimiento = MovimientoInventario(
            producto = producto,
            tipo = tipo,
            cantidad = cantidad,
            motivo = motivo,
            usuario = usuario
        )

        val guardado = movimientoInventarioRepository.save(movimiento)
        servicioAuditoria.registrar(responsable, AccionAuditoria.CREACION, "MovimientoInventario", requireNotNull(guardado.id))
        return guardado
    }

    fun obtenerMovimientosPorProducto(productoId: Long): List<MovimientoInventarioResponse> =
        movimientoInventarioRepository.findAllByProductoIdOrderByFechaDesc(productoId).map { it.toResponse() }

    private fun MovimientoInventario.toResponse() = MovimientoInventarioResponse(
        id = requireNotNull(id),
        productoId = requireNotNull(producto.id),
        nombreProducto = producto.nombre,
        tipo = tipo,
        cantidad = cantidad,
        motivo = motivo,
        stockResultante = producto.stockActual,
        correoUsuario = usuario.correo,
        fecha = fecha
    )
}

package com.accesorios.gestion.service

import com.accesorios.gestion.dto.venta.VentaRequest
import com.accesorios.gestion.dto.venta.VentaResponse
import com.accesorios.gestion.exception.CredencialesInvalidasException
import com.accesorios.gestion.exception.RecursoNoEncontradoException
import com.accesorios.gestion.exception.TransicionEstadoInvalidaException
import com.accesorios.gestion.model.AccionAuditoria
import com.accesorios.gestion.model.DetalleVenta
import com.accesorios.gestion.model.EstadoVenta
import com.accesorios.gestion.model.TipoMovimientoInventario
import com.accesorios.gestion.model.Venta
import com.accesorios.gestion.repository.UsuarioRepository
import com.accesorios.gestion.repository.VentaRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class ServicioVenta(
    private val ventaRepository: VentaRepository,
    private val servicioCliente: ServicioCliente,
    private val servicioProducto: ServicioProducto,
    private val usuarioRepository: UsuarioRepository,
    private val servicioInventario: ServicioInventario,
    private val servicioFactura: ServicioFactura,
    private val servicioAuditoria: ServicioAuditoria
) {

    companion object {
        private val TASA_IVA = BigDecimal("0.19")
    }

    fun crear(request: VentaRequest, responsable: String): VentaResponse {
        val cliente = servicioCliente.obtenerPorIdOLanzar(request.clienteId)
        val vendedor = usuarioRepository.findByCorreo(responsable)
            ?: throw CredencialesInvalidasException("Usuario no encontrado")

        var subtotal = BigDecimal.ZERO
        val detalles = mutableListOf<DetalleVenta>()

        val venta = Venta(
            cliente = cliente,
            vendedor = vendedor,
            subtotal = BigDecimal.ZERO,
            iva = BigDecimal.ZERO,
            total = BigDecimal.ZERO
        )

        request.items.forEach { item ->
            val producto = servicioProducto.obtenerEntidadActivaOLanzar(item.productoId)
            val totalLinea = producto.precio.multiply(BigDecimal(item.cantidad))
            subtotal = subtotal.add(totalLinea)

            detalles.add(
                DetalleVenta(
                    venta = venta,
                    producto = producto,
                    cantidad = item.cantidad,
                    precioUnitario = producto.precio
                )
            )
        }

        val iva = subtotal.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP)
        val total = subtotal.add(iva)

        venta.subtotal = subtotal.setScale(2, RoundingMode.HALF_UP)
        venta.iva = iva
        venta.total = total
        venta.detalles = detalles

        val guardada = ventaRepository.save(venta)
        servicioAuditoria.registrar(responsable, AccionAuditoria.CREACION, "Venta", requireNotNull(guardada.id))
        return guardada.toVentaResponse()
    }

    /** Confirma la venta: descuenta stock (RF-04) y genera la factura (RF-07). */
    fun completar(id: Long, responsable: String): VentaResponse {
        val venta = buscarPorIdOLanzar(id)

        if (venta.estado != EstadoVenta.PENDIENTE) {
            throw TransicionEstadoInvalidaException("Solo una venta pendiente puede completarse")
        }

        venta.detalles.forEach { detalle ->
            servicioInventario.aplicarCambioStock(
                producto = detalle.producto,
                cantidad = detalle.cantidad,
                tipo = TipoMovimientoInventario.SALIDA,
                motivo = "Venta #${venta.id}",
                responsable = responsable
            )
        }

        venta.estado = EstadoVenta.COMPLETADA
        val guardada = ventaRepository.save(venta)
        servicioFactura.generarParaVenta(guardada)

        servicioAuditoria.registrar(responsable, AccionAuditoria.ACTUALIZACION, "Venta", id)
        return guardada.toVentaResponse()
    }

    fun anular(id: Long, responsable: String): VentaResponse {
        val venta = buscarPorIdOLanzar(id)

        if (venta.estado != EstadoVenta.PENDIENTE) {
            throw TransicionEstadoInvalidaException("Solo una venta pendiente puede anularse")
        }

        venta.estado = EstadoVenta.ANULADA
        val guardada = ventaRepository.save(venta)
        servicioAuditoria.registrar(responsable, AccionAuditoria.ACTUALIZACION, "Venta", id)
        return guardada.toVentaResponse()
    }

    fun obtenerPorId(id: Long): VentaResponse = buscarPorIdOLanzar(id).toVentaResponse()

    fun obtenerHistorialCompras(clienteId: Long): List<VentaResponse> {
        servicioCliente.obtenerPorIdOLanzar(clienteId)
        return ventaRepository.findAllByClienteIdOrderByFechaCreacionDesc(clienteId).map { it.toVentaResponse() }
    }

    private fun buscarPorIdOLanzar(id: Long): Venta =
        ventaRepository.findById(id)
            .orElseThrow { RecursoNoEncontradoException("Venta no encontrada") }
}

package com.accesorios.gestion.service

import com.accesorios.gestion.dto.venta.DetalleVentaResponse
import com.accesorios.gestion.dto.venta.VentaResponse
import com.accesorios.gestion.model.Venta

/** Mapper compartido entre ServicioVenta y ServicioFactura para evitar una dependencia circular. */
fun Venta.toVentaResponse(): VentaResponse = VentaResponse(
    id = requireNotNull(id),
    clienteId = requireNotNull(cliente.id),
    nombreCliente = cliente.nombre,
    vendedorId = requireNotNull(vendedor.id),
    correoVendedor = vendedor.correo,
    subtotal = subtotal,
    iva = iva,
    total = total,
    estado = estado,
    detalles = detalles.map {
        DetalleVentaResponse(
            productoId = requireNotNull(it.producto.id),
            nombreProducto = it.producto.nombre,
            cantidad = it.cantidad,
            precioUnitario = it.precioUnitario,
            totalLinea = it.precioUnitario.multiply(java.math.BigDecimal(it.cantidad))
        )
    },
    fechaCreacion = fechaCreacion
)

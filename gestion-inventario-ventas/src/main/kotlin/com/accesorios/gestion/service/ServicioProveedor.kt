package com.accesorios.gestion.service

import com.accesorios.gestion.dto.proveedor.ProveedorRequest
import com.accesorios.gestion.dto.proveedor.ProveedorResponse
import com.accesorios.gestion.exception.RecursoDuplicadoException
import com.accesorios.gestion.exception.RecursoNoEncontradoException
import com.accesorios.gestion.model.AccionAuditoria
import com.accesorios.gestion.model.Proveedor
import com.accesorios.gestion.repository.ProveedorRepository
import org.springframework.stereotype.Service

@Service
class ServicioProveedor(
    private val proveedorRepository: ProveedorRepository,
    private val servicioAuditoria: ServicioAuditoria
) {

    fun crear(request: ProveedorRequest, responsable: String): ProveedorResponse {
        if (proveedorRepository.existsByNombre(request.nombre.trim())) {
            throw RecursoDuplicadoException("Ya existe un proveedor con ese nombre")
        }

        val proveedor = Proveedor(
            nombre = request.nombre.trim(),
            nombreContacto = request.nombreContacto?.trim(),
            telefono = request.telefono?.trim(),
            correo = request.correo?.trim()?.lowercase()
        )

        val guardado = proveedorRepository.save(proveedor)
        servicioAuditoria.registrar(responsable, AccionAuditoria.CREACION, "Proveedor", requireNotNull(guardado.id))
        return guardado.toResponse()
    }

    fun actualizar(id: Long, request: ProveedorRequest, responsable: String): ProveedorResponse {
        val proveedor = buscarPorIdOLanzar(id)

        if (proveedorRepository.existsByNombreAndIdNot(request.nombre.trim(), id)) {
            throw RecursoDuplicadoException("Ya existe un proveedor con ese nombre")
        }

        proveedor.nombre = request.nombre.trim()
        proveedor.nombreContacto = request.nombreContacto?.trim()
        proveedor.telefono = request.telefono?.trim()
        proveedor.correo = request.correo?.trim()?.lowercase()

        val guardado = proveedorRepository.save(proveedor)
        servicioAuditoria.registrar(responsable, AccionAuditoria.ACTUALIZACION, "Proveedor", id)
        return guardado.toResponse()
    }

    fun eliminar(id: Long, responsable: String) {
        val proveedor = buscarPorIdOLanzar(id)
        proveedorRepository.delete(proveedor)
        servicioAuditoria.registrar(responsable, AccionAuditoria.ELIMINACION, "Proveedor", id)
    }

    fun obtenerTodos(): List<ProveedorResponse> = proveedorRepository.findAll().map { it.toResponse() }

    fun obtenerPorIdOLanzar(id: Long): Proveedor = buscarPorIdOLanzar(id)

    private fun buscarPorIdOLanzar(id: Long): Proveedor =
        proveedorRepository.findById(id)
            .orElseThrow { RecursoNoEncontradoException("Proveedor no encontrado") }

    private fun Proveedor.toResponse() = ProveedorResponse(
        id = requireNotNull(id),
        nombre = nombre,
        nombreContacto = nombreContacto,
        telefono = telefono,
        correo = correo
    )
}

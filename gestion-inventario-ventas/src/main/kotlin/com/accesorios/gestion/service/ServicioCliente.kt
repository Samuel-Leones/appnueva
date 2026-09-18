package com.accesorios.gestion.service

import com.accesorios.gestion.dto.cliente.ClienteRequest
import com.accesorios.gestion.dto.cliente.ClienteResponse
import com.accesorios.gestion.dto.cliente.ClienteUpdateRequest
import com.accesorios.gestion.exception.OperacionNoPermitidaException
import com.accesorios.gestion.exception.RecursoDuplicadoException
import com.accesorios.gestion.exception.RecursoNoEncontradoException
import com.accesorios.gestion.model.AccionAuditoria
import com.accesorios.gestion.model.Cliente
import com.accesorios.gestion.repository.ClienteRepository
import com.accesorios.gestion.repository.VentaRepository
import org.springframework.stereotype.Service

@Service
class ServicioCliente(
    private val clienteRepository: ClienteRepository,
    private val ventaRepository: VentaRepository,
    private val servicioAuditoria: ServicioAuditoria
) {

    fun crear(request: ClienteRequest, responsable: String): ClienteResponse {

        if (clienteRepository.existsByDocumento(request.documento.trim())) {
            throw RecursoDuplicadoException("Ya existe un cliente con ese documento")
        }

        val cliente = Cliente(
            nombre = request.nombre.trim(),
            documento = request.documento.trim(),
            telefono = request.telefono?.trim(),
            correo = request.correo?.trim()?.lowercase(),
            direccion = request.direccion?.trim()
        )

        val guardado = clienteRepository.save(cliente)

        servicioAuditoria.registrar(
            responsable,
            AccionAuditoria.CREACION,
            "Cliente",
            requireNotNull(guardado.id)
        )

        return guardado.toResponse()
    }

    fun actualizar(
        id: Long,
        request: ClienteUpdateRequest,
        responsable: String
    ): ClienteResponse {

        val cliente = buscarPorIdOLanzar(id)

        request.documento?.trim()?.let { nuevoDocumento ->

            if (clienteRepository.existsByDocumentoAndIdNot(nuevoDocumento, id)) {
                throw RecursoDuplicadoException("Ya existe un cliente con ese documento")
            }

            cliente.documento = nuevoDocumento
        }

        request.nombre?.trim()?.let {
            cliente.nombre = it
        }

        request.telefono?.trim()?.let {
            cliente.telefono = it
        }

        request.correo?.trim()?.lowercase()?.let {
            cliente.correo = it
        }

        request.direccion?.trim()?.let {
            cliente.direccion = it
        }

        val guardado = clienteRepository.save(cliente)

        servicioAuditoria.registrar(
            responsable,
            AccionAuditoria.ACTUALIZACION,
            "Cliente",
            id
        )

        return guardado.toResponse()
    }

    fun eliminar(
        id: Long,
        responsable: String
    ) {

        val cliente = buscarPorIdOLanzar(id)

        if (ventaRepository.existsByClienteId(id)) {
            throw OperacionNoPermitidaException(
                "No se puede eliminar el cliente porque tiene ventas registradas."
            )
        }

        clienteRepository.delete(cliente)

        servicioAuditoria.registrar(
            responsable,
            AccionAuditoria.ELIMINACION,
            "Cliente",
            id
        )
    }

    fun obtenerTodos(): List<ClienteResponse> =
        clienteRepository.findAll().map { it.toResponse() }

    fun buscar(termino: String): List<ClienteResponse> =
        clienteRepository
            .findAllByNombreContainingIgnoreCaseOrDocumentoContainingIgnoreCase(
                termino,
                termino
            )
            .map { it.toResponse() }

    fun obtenerPorIdOLanzar(id: Long): Cliente =
        buscarPorIdOLanzar(id)

    private fun buscarPorIdOLanzar(id: Long): Cliente =
        clienteRepository.findById(id)
            .orElseThrow {
                RecursoNoEncontradoException("Cliente no encontrado")
            }

    private fun Cliente.toResponse() = ClienteResponse(
        id = requireNotNull(id),
        nombre = nombre,
        documento = documento,
        telefono = telefono,
        correo = correo,
        direccion = direccion,
        fechaRegistro = fechaRegistro
    )
}
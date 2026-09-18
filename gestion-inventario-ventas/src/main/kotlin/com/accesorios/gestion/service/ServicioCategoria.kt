package com.accesorios.gestion.service

import com.accesorios.gestion.dto.categoria.CategoriaRequest
import com.accesorios.gestion.dto.categoria.CategoriaResponse
import com.accesorios.gestion.dto.categoria.CategoriaUpdateRequest
import com.accesorios.gestion.exception.OperacionNoPermitidaException
import com.accesorios.gestion.exception.RecursoDuplicadoException
import com.accesorios.gestion.exception.RecursoNoEncontradoException
import com.accesorios.gestion.model.AccionAuditoria
import com.accesorios.gestion.model.Categoria
import com.accesorios.gestion.model.EstadoCategoria
import com.accesorios.gestion.repository.CategoriaRepository
import com.accesorios.gestion.repository.ProductoRepository
import org.springframework.stereotype.Service

@Service
class ServicioCategoria(
    private val categoriaRepository: CategoriaRepository,
    private val productoRepository: ProductoRepository,
    private val servicioAuditoria: ServicioAuditoria
) {

    fun crear(request: CategoriaRequest, responsable: String): CategoriaResponse {

        if (categoriaRepository.existsByNombreAndEstadoNot(request.nombre.trim(), EstadoCategoria.ELIMINADA)) {
            throw RecursoDuplicadoException("Ya existe una categoría con ese nombre")
        }

        val categoria = Categoria(
            nombre = request.nombre.trim(),
            descripcion = request.descripcion?.trim(),
            estado = EstadoCategoria.ACTIVA
        )

        val guardada = categoriaRepository.save(categoria)

        servicioAuditoria.registrar(
            responsable,
            AccionAuditoria.CREACION,
            "Categoria",
            requireNotNull(guardada.id)
        )

        return guardada.toResponse()
    }

    fun actualizar(
        id: Long,
        request: CategoriaUpdateRequest,
        responsable: String
    ): CategoriaResponse {

        val categoria = buscarPorIdOLanzar(id)

        if (categoria.estado == EstadoCategoria.ELIMINADA) {
            throw OperacionNoPermitidaException(
                "No se puede actualizar una categoría eliminada."
            )
        }

        request.nombre?.trim()?.let { nuevoNombre ->

            if (categoriaRepository.existsByNombreAndIdNotAndEstadoNot(nuevoNombre, id, EstadoCategoria.ELIMINADA)) {
                throw RecursoDuplicadoException("Ya existe una categoría con ese nombre")
            }

            categoria.nombre = nuevoNombre
        }

        request.descripcion?.trim()?.let {
            categoria.descripcion = it
        }

        request.estado?.let { nuevoEstado ->
            if (nuevoEstado == EstadoCategoria.ELIMINADA) {
                throw OperacionNoPermitidaException(
                    "Use la operación de eliminación para cambiar una categoría a este estado."
                )
            }
            categoria.estado = nuevoEstado
        }

        val guardada = categoriaRepository.save(categoria)

        servicioAuditoria.registrar(
            responsable,
            AccionAuditoria.ACTUALIZACION,
            "Categoria",
            id
        )

        return guardada.toResponse()
    }

    fun eliminar(id: Long, responsable: String) {

        val categoria = buscarPorIdOLanzar(id)

        if (categoria.estado == EstadoCategoria.ELIMINADA) {
            throw OperacionNoPermitidaException(
                "La categoría ya se encuentra eliminada."
            )
        }

        // Eliminación lógica: se cambia el estado en lugar de borrar el registro,
        // así se evitan errores de integridad referencial cuando existen productos
        // (u otras entidades) que dependen de esta categoría.
        categoria.estado = EstadoCategoria.ELIMINADA
        categoriaRepository.save(categoria)

        servicioAuditoria.registrar(
            responsable,
            AccionAuditoria.ELIMINACION,
            "Categoria",
            id
        )
    }

    fun obtenerTodas(): List<CategoriaResponse> =
        categoriaRepository.findByEstadoNot(EstadoCategoria.ELIMINADA).map { it.toResponse() }

    fun obtenerPorIdOLanzar(id: Long): Categoria =
        buscarPorIdOLanzar(id)

    private fun buscarPorIdOLanzar(id: Long): Categoria =
        categoriaRepository.findById(id)
            .filter { it.estado != EstadoCategoria.ELIMINADA }
            .orElseThrow {
                RecursoNoEncontradoException("Categoría no encontrada")
            }

    private fun Categoria.toResponse() = CategoriaResponse(
        id = requireNotNull(id),
        nombre = nombre,
        descripcion = descripcion,
        estado = estado
    )
}

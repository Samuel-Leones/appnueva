package com.accesorios.gestion.service

import com.accesorios.gestion.dto.auditoria.RegistroAuditoriaResponse
import com.accesorios.gestion.model.AccionAuditoria
import com.accesorios.gestion.model.RegistroAuditoria
import com.accesorios.gestion.repository.RegistroAuditoriaRepository
import com.accesorios.gestion.repository.UsuarioRepository
import org.springframework.stereotype.Service


@Service
class ServicioAuditoria(
    private val registroAuditoriaRepository: RegistroAuditoriaRepository,
    private val usuarioRepository: UsuarioRepository
) {

    fun registrar(correoResponsable: String, accion: AccionAuditoria, entidad: String, idEntidad: Long) {
        val usuario = usuarioRepository.findByCorreo(correoResponsable) ?: return

        val registro = RegistroAuditoria(
            usuarioId = requireNotNull(usuario.id),
            correoUsuario = usuario.correo,
            accion = accion,
            entidadAfectada = entidad,
            idEntidadAfectada = idEntidad
        )
        registroAuditoriaRepository.save(registro)
    }

    fun obtenerRegistros(entidad: String?): List<RegistroAuditoriaResponse> {
        val registros = if (entidad.isNullOrBlank()) {
            registroAuditoriaRepository.findAllByOrderByFechaHoraDesc()
        } else {
            registroAuditoriaRepository.findAllByEntidadAfectadaOrderByFechaHoraDesc(entidad)
        }
        return registros.map { it.toResponse() }
    }

    private fun RegistroAuditoria.toResponse() = RegistroAuditoriaResponse(
        id = requireNotNull(id),
        usuarioId = usuarioId,
        correoUsuario = correoUsuario,
        accion = accion,
        entidadAfectada = entidadAfectada,
        idEntidadAfectada = idEntidadAfectada,
        fechaHora = fechaHora
    )
}

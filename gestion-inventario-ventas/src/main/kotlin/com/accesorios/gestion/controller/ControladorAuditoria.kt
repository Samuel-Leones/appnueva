package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.auditoria.RegistroAuditoriaResponse
import com.accesorios.gestion.service.ServicioAuditoria
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auditoria")
class ControladorAuditoria(
    private val servicioAuditoria: ServicioAuditoria
) {

    @GetMapping
    fun listar(@RequestParam(required = false) entidad: String?): List<RegistroAuditoriaResponse> {
        return servicioAuditoria.obtenerRegistros(entidad)
    }
}

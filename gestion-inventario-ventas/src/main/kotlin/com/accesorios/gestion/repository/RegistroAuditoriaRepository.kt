package com.accesorios.gestion.repository

import com.accesorios.gestion.model.RegistroAuditoria
import org.springframework.data.jpa.repository.JpaRepository

interface RegistroAuditoriaRepository : JpaRepository<RegistroAuditoria, Long> {
    fun findAllByOrderByFechaHoraDesc(): List<RegistroAuditoria>
    fun findAllByEntidadAfectadaOrderByFechaHoraDesc(entidadAfectada: String): List<RegistroAuditoria>
}

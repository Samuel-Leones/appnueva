package com.accesorios.gestion.repository

import com.accesorios.gestion.model.Cliente
import org.springframework.data.jpa.repository.JpaRepository

interface ClienteRepository : JpaRepository<Cliente, Long> {
    fun existsByDocumento(documento: String): Boolean
    fun existsByDocumentoAndIdNot(documento: String, id: Long): Boolean
    fun findAllByNombreContainingIgnoreCaseOrDocumentoContainingIgnoreCase(nombre: String, documento: String): List<Cliente>
}

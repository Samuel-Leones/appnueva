package com.accesorios.gestion.repository

import com.accesorios.gestion.model.Proveedor
import org.springframework.data.jpa.repository.JpaRepository

interface ProveedorRepository : JpaRepository<Proveedor, Long> {
    fun existsByNombre(nombre: String): Boolean
    fun existsByNombreAndIdNot(nombre: String, id: Long): Boolean
}

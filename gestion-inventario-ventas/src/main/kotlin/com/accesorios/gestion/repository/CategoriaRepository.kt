package com.accesorios.gestion.repository

import com.accesorios.gestion.model.Categoria
import com.accesorios.gestion.model.EstadoCategoria
import org.springframework.data.jpa.repository.JpaRepository

interface CategoriaRepository : JpaRepository<Categoria, Long> {

    fun existsByNombreAndEstadoNot(nombre: String, estado: EstadoCategoria): Boolean

    fun existsByNombreAndIdNotAndEstadoNot(nombre: String, id: Long, estado: EstadoCategoria): Boolean

    fun findByEstadoNot(estado: EstadoCategoria): List<Categoria>
}

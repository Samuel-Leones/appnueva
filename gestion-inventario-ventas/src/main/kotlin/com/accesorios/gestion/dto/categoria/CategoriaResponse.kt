package com.accesorios.gestion.dto.categoria

import com.accesorios.gestion.model.EstadoCategoria

data class CategoriaResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val estado: EstadoCategoria
)

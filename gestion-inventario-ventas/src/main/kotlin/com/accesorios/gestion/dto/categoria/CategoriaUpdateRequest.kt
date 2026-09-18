package com.accesorios.gestion.dto.categoria

import com.accesorios.gestion.model.EstadoCategoria

data class CategoriaUpdateRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val estado: EstadoCategoria? = null
)

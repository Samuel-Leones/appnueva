package com.accesorios.gestion.dto.cliente

data class ClienteUpdateRequest(
    val nombre: String? = null,
    val documento: String? = null,
    val telefono: String? = null,
    val correo: String? = null,
    val direccion: String? = null
    )


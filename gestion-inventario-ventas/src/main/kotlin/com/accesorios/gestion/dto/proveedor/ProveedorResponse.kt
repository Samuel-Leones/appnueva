package com.accesorios.gestion.dto.proveedor

data class ProveedorResponse(
    val id: Long,
    val nombre: String,
    val nombreContacto: String?,
    val telefono: String?,
    val correo: String?
)

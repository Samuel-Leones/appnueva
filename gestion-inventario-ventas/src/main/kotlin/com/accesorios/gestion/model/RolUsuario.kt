package com.accesorios.gestion.model

enum class RolUsuario {
    SUPER_USUARIO,
    ADMINISTRADOR,
    EMPLEADO;

    /** SUPER_USUARIO hereda los permisos de ADMINISTRADOR y EMPLEADO; ADMINISTRADOR hereda los de EMPLEADO. */
    fun rolesImplicitos(): List<RolUsuario> = when (this) {
        SUPER_USUARIO -> listOf(SUPER_USUARIO, ADMINISTRADOR, EMPLEADO)
        ADMINISTRADOR -> listOf(ADMINISTRADOR, EMPLEADO)
        EMPLEADO -> listOf(EMPLEADO)
    }
}

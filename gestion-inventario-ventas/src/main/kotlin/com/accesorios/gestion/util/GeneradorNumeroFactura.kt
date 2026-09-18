package com.accesorios.gestion.util

object GeneradorNumeroFactura {

    private const val PREFIJO = "FAC-"

    fun generar(secuencia: Long): String = "$PREFIJO${secuencia.toString().padStart(6, '0')}"
}

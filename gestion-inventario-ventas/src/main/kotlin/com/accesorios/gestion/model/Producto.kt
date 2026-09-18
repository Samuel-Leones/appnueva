package com.accesorios.gestion.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "productos")
class Producto(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var nombre: String,

    var descripcion: String? = null,

    @Column(nullable = false, unique = true)
    var codigo: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    var categoria: Categoria,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    var proveedor: Proveedor? = null,

    @Column(nullable = false)
    var precio: BigDecimal,

    @Column(nullable = false)
    var stockActual: Int = 0,

    @Column(nullable = false)
    var stockMinimo: Int = 0,

    @Column(nullable = false)
    var activo: Boolean = true,

    @Column(nullable = false, updatable = false)
    var fechaCreacion: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var fechaActualizacion: LocalDateTime = LocalDateTime.now()
)

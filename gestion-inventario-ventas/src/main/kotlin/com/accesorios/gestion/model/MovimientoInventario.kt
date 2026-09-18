package com.accesorios.gestion.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "movimientos_inventario")
class MovimientoInventario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    var producto: Producto,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tipo: TipoMovimientoInventario,

    @Column(nullable = false)
    var cantidad: Int,

    var motivo: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuario,

    @Column(nullable = false)
    var fecha: LocalDateTime = LocalDateTime.now()
)

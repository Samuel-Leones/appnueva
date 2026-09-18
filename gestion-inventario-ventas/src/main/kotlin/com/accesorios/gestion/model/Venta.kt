package com.accesorios.gestion.model

import jakarta.persistence.CascadeType
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
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "ventas")
class Venta(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    var cliente: Cliente,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var vendedor: Usuario,

    @Column(nullable = false)
    var subtotal: BigDecimal,

    @Column(nullable = false)
    var iva: BigDecimal,

    @Column(nullable = false)
    var total: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var estado: EstadoVenta = EstadoVenta.PENDIENTE,

    @OneToMany(mappedBy = "venta", cascade = [CascadeType.ALL], orphanRemoval = true)
    var detalles: MutableList<DetalleVenta> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    var fechaCreacion: LocalDateTime = LocalDateTime.now()
)

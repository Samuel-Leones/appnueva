package com.accesorios.gestion.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "clientes")
class Cliente(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var nombre: String,

    @Column(nullable = false, unique = true)
    var documento: String,

    var telefono: String? = null,

    var correo: String? = null,

    var direccion: String? = null,

    @Column(nullable = false, updatable = false)
    var fechaRegistro: LocalDateTime = LocalDateTime.now()
)

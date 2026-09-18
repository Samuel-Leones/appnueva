package com.accesorios.gestion.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "usuarios")
class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var correo: String,

    @Column(nullable = false)
    var nombreCompleto: String,

    @Column(nullable = false)
    var contrasena: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var rol: RolUsuario = RolUsuario.EMPLEADO,

    @Column(nullable = false)
    var activo: Boolean = true,

    @Column(nullable = false, updatable = false)
    var fechaCreacion: LocalDateTime = LocalDateTime.now()
)

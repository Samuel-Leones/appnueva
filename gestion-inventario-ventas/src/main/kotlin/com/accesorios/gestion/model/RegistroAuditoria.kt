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
@Table(name = "registros_auditoria")
class RegistroAuditoria(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var usuarioId: Long,

    @Column(nullable = false)
    var correoUsuario: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var accion: AccionAuditoria,

    @Column(nullable = false)
    var entidadAfectada: String,

    @Column(nullable = false)
    var idEntidadAfectada: Long,

    @Column(nullable = false)
    var fechaHora: LocalDateTime = LocalDateTime.now()
)

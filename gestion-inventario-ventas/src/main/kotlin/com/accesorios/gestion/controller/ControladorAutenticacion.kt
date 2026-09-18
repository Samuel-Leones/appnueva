package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.autenticacion.LoginRequest
import com.accesorios.gestion.dto.autenticacion.RegistroRequest
import com.accesorios.gestion.dto.autenticacion.TokenResponse
import com.accesorios.gestion.dto.autenticacion.UsuarioResponse
import com.accesorios.gestion.service.ServicioAutenticacion
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/autenticacion")
class ControladorAutenticacion(
    private val servicioAutenticacion: ServicioAutenticacion
) {

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/registro")
    fun registrar(
        @Valid @RequestBody request: RegistroRequest,
        authentication: Authentication
    ): ResponseEntity<UsuarioResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(servicioAutenticacion.registrar(request, authentication.name))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(servicioAutenticacion.login(request))
    }
}

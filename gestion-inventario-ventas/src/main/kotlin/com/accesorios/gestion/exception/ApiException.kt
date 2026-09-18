package com.accesorios.gestion.exception

import org.springframework.http.HttpStatus

open class ApiException(
    message: String,
    val status: HttpStatus
) : RuntimeException(message)

class RecursoDuplicadoException(message: String) :
    ApiException(message, HttpStatus.CONFLICT)

class RecursoNoEncontradoException(message: String) :
    ApiException(message, HttpStatus.NOT_FOUND)

class CredencialesInvalidasException(message: String) :
    ApiException(message, HttpStatus.UNAUTHORIZED)

class SolicitudInvalidaException(message: String) :
    ApiException(message, HttpStatus.BAD_REQUEST)

class StockInsuficienteException(message: String) :
    ApiException(message, HttpStatus.BAD_REQUEST)

class TransicionEstadoInvalidaException(message: String) :
    ApiException(message, HttpStatus.BAD_REQUEST)

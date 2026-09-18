package com.accesorios.gestion.exception

import org.springframework.http.HttpStatus

class OperacionNoPermitidaException(
    message: String
) : ApiException(message, HttpStatus.CONFLICT)
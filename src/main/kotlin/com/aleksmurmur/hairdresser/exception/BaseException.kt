package com.aleksmurmur.hairdresser.exception

import org.springframework.http.HttpStatus

abstract class BaseException(
    message: String = "",
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    open val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
}
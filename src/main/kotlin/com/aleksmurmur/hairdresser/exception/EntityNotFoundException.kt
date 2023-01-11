package com.aleksmurmur.hairdresser.exception

import org.springframework.http.HttpStatus

class EntityNotFoundException(message: String) : BaseException(message) {
    override val httpStatus: HttpStatus
        get() = HttpStatus.NOT_FOUND
}
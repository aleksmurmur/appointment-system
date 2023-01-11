package com.aleksmurmur.hairdresser.exception

import org.springframework.http.HttpStatus

class ValidationException(message: String) : BaseException(message) {
    override val httpStatus: HttpStatus
        get() = HttpStatus.BAD_REQUEST

}
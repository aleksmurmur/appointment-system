package com.aleksmurmur.hairdresser.exception

import org.springframework.http.HttpStatus

class UnavailableActionException(message: String) : BaseException(message) {
    override val httpStatus: HttpStatus
        get() = HttpStatus.BAD_REQUEST
}
package com.aleksmurmur.hairdresser.api

import com.aleksmurmur.hairdresser.exception.BaseException
import com.aleksmurmur.hairdresser.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.*


@RestControllerAdvice
class ExceptionHandler {

    private companion object : KLogging() {
        //const val VALIDATION_ERROR_MESSAGE = "Validation error"
    }

    @ExceptionHandler(BaseException::class)
    fun baseExceptionHandler(exception: BaseException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        log(exception, request)
        return ResponseEntity
            .status(exception.httpStatus)
            .body(ErrorResponse(exception.httpStatus.name, exception.message ?: ""))
    }

    private fun log(exception: BaseException, request: HttpServletRequest) {

        val user =  request.getHeader("X-Forwarded-For")?.let { StringTokenizer(it, ",").nextToken().trim() }
            ?: request.remoteAddr

        logger.warn {
        """
            - exception: ${exception.message}
            - user: $user
            - requestMethod: ${request.method}
            - requestUri: ${request.requestURI}
        """
        }
    }


}
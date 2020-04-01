package com.example.library.rest

import com.example.library.exception.BadRequestException
import com.example.library.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionHandler {

    private val logger = LoggerFactory.getLogger(com.example.library.rest.ExceptionHandler::class.java)

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(exception: BadRequestException, request: HttpServletRequest): ResponseEntity<String> {
        val status = HttpStatus.BAD_REQUEST
        logException(exception, status, request)
        return buildResponse(
                status,
                exception.message
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(exception: NotFoundException, request: HttpServletRequest): ResponseEntity<String> {
        val status = HttpStatus.NOT_FOUND
        logException(exception, status, request)
        return buildResponse(
                status,
                exception.message
        )
    }

    private fun buildResponse(status: HttpStatus, payload: String? = null): ResponseEntity<String> {
        return ResponseEntity.status(status).body(payload)
    }

    private fun logException(exception: Throwable, status: HttpStatus, request: HttpServletRequest) {
        logger.warn("Returning HTTP " + status.toString() +
                    " caused by a " + request.method.toString() +
                    " request at " + request.requestURI.toString() +
                    " with error message:  " + exception.message
        )
    }

}
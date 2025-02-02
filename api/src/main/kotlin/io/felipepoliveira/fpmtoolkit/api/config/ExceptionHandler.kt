package io.felipepoliveira.fpmtoolkit.api.config

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.api.ext.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import kotlin.Exception

@ControllerAdvice
class ExceptionHandler {

    /**
     * Catch errors related to client data deserialization
     */
    @ExceptionHandler(exception = [HttpMessageNotReadableException::class])
    fun badRequest(exception: Exception): ResponseEntity<Any> {
        return ResponseEntity
            .badRequest()
            .header("X-Reason", "You sent a request that could not be decoded by the server. Check the Content-Type of the endpoint to see the details of the request that should be sent")
            .build()
    }

    /**
     * Handles BusinessRuleException in the platform
     */
    @ExceptionHandler(exception = [BusinessRuleException::class])
    fun businessRuleException(exception: BusinessRuleException): ResponseEntity<Any>  = exception.toResponseEntity()

    /**
     * Catch any other exception
     */
    @ExceptionHandler(exception = [Exception::class])
    fun exception(exception: Exception): ResponseEntity<Any> {
        exception.printStackTrace()
        return ResponseEntity
            .status(500)
            .build()
    }

}
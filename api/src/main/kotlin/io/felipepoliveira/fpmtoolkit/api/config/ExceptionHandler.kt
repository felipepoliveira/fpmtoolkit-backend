package io.felipepoliveira.fpmtoolkit.api.config

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.api.ext.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.resource.NoResourceFoundException
import kotlin.Exception

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(exception = [AccessDeniedException::class])
    fun accessDenied(exception: AccessDeniedException): ResponseEntity<Any> {
        return ResponseEntity
            .status(403)
            .header("X-Reason", "You do not have the required role to use this service")
            .build()
    }

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

    @ExceptionHandler(exception = [NoResourceFoundException::class])
    fun noResourceFoundException(exception: NoResourceFoundException): ResponseEntity<Any> {
        return ResponseEntity
            .notFound()
            .header("X-Reason", "The endpoint ${exception.resourcePath} did not match any mapped route in the server")
            .build()
    }

}
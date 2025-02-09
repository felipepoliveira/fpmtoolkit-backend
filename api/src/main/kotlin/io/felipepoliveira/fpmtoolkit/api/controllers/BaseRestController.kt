package io.felipepoliveira.fpmtoolkit.api.controllers

import org.springframework.http.ResponseEntity

typealias EmptyResponseBuilderCallback = (responseBuilder: ResponseEntity.BodyBuilder) -> Unit
typealias ResponseBuilderCallbackWithBody = (responseBuilder: ResponseEntity.BodyBuilder) -> Any
typealias OptionalResponseBuilderCallback = (responseBuilder: ResponseEntity.BodyBuilder) -> Any?

abstract class BaseRestController {

    /**
     * Functional method that increase the syntax quality for @RestController endpoints. Send an 204 NO CONTENT response
     * with no response body
     */
    fun noContent(callback: EmptyResponseBuilderCallback) = send(204, callback)

    /**
     * Functional method that increase the syntax quality for @RestController endpoints. Send an 200 OK response
     * with a body
     */
    fun ok(callback: ResponseBuilderCallbackWithBody) = send(200, callback)

    /**
     * Functional method that increase the syntax quality for @RestController endpoints.
     */
    fun send(responseCode: Int, callback: OptionalResponseBuilderCallback): ResponseEntity<Any> {
        val responseBuilder = ResponseEntity.status(responseCode)
        val body = callback(responseBuilder)
        return if (body != null) {
            responseBuilder.body(body)
        } else {
            responseBuilder.build()
        }
    }

}
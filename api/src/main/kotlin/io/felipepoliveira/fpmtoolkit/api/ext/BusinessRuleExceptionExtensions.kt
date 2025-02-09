package io.felipepoliveira.fpmtoolkit.api.ext

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import org.springframework.http.ResponseEntity

private fun fromBusinessRuleTypeToHttpStatus(errorType: BusinessRulesError): Int {
    return when(errorType) {
        BusinessRulesError.EMAIL_NOT_CONFIRMED -> 403
        BusinessRulesError.FORBIDDEN -> 403
        BusinessRulesError.INVALID_CREDENTIALS -> 403
        BusinessRulesError.INVALID_EMAIL -> 403
        BusinessRulesError.INVALID_PASSWORD -> 403
        BusinessRulesError.NOT_FOUND -> 404
        BusinessRulesError.VALIDATION -> 422
    }
}

fun BusinessRuleException.toResponseEntity(): ResponseEntity<Any> {
    return ResponseEntity
        .status(fromBusinessRuleTypeToHttpStatus(this.error))
        .header("X-Error", this.error.toString())
        .header("X-Reason", this.reason)
        .body(this.details)
}
package io.felipepoliveira.fpmtoolkit.api.ext

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import org.springframework.http.ResponseEntity

private fun fromBusinessRuleTypeToHttpStatus(errorType: BusinessRulesError): Int {
    return when(errorType) {
        BusinessRulesError.NotFound -> 404
        BusinessRulesError.InvalidEmail -> 403
        BusinessRulesError.InvalidPassword -> 403
        BusinessRulesError.Validation -> 422
    }
}

fun BusinessRuleException.toResponseEntity(): ResponseEntity<Any> {
    return ResponseEntity
        .status(fromBusinessRuleTypeToHttpStatus(this.error))
        .header("X-Reason", this.reason)
        .body(this.details)
}
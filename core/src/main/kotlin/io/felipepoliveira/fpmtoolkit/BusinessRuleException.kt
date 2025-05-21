package io.felipepoliveira.fpmtoolkit

import io.felipepoliveira.fpmtoolkit.ext.toMap
import org.springframework.validation.BindingResult


class BusinessRuleException(
    val error: BusinessRulesError,
    val reason: String,
    val details: Map<String, String>? = null
) : Exception(reason) {

    /**
     * Create an easy-to-use constructor for a BusinessRuleException based on the data of a BindingResult
     */
    constructor(bindingResult: BindingResult, reason: String = "Validation error") :
            this(BusinessRulesError.VALIDATION, reason, bindingResult.toMap())

}

enum class BusinessRulesError {

    DUPLICATED,
    EMAIL_NOT_CONFIRMED,
    FORBIDDEN,
    INVALID_CREDENTIALS,
    INVALID_EMAIL,
    INVALID_PARAMETERS,
    INVALID_PASSWORD,
    NOT_FOUND,
    PAYMENT_REQUIRED,
    TOO_MANY_REQUESTS,
    VALIDATION,
}
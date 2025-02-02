package io.felipepoliveira.fpmtoolkit

import io.felipepoliveira.fpmtoolkit.ext.toMap
import org.springframework.validation.BindingResult
import java.lang.Exception


class BusinessRuleException(
    val error: BusinessRulesError,
    val reason: String,
    val details: Map<String, String>? = null
) : Exception(reason) {

    /**
     * Create an easy-to-use constructor for a BusinessRuleException based on the data of a BindingResult
     */
    constructor(bindingResult: BindingResult, reason: String = "Validation error") :
            this(BusinessRulesError.Validation, reason, bindingResult.toMap())

}

enum class BusinessRulesError {
    InvalidEmail,
    InvalidPassword,
    NotFound,
    Validation,
}
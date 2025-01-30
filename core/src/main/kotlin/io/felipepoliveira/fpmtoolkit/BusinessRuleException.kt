package io.felipepoliveira.fpmtoolkit

import io.felipepoliveira.fpmtoolkit.ext.toMap
import org.springframework.validation.BindingResult
import java.lang.Exception


class BusinessRuleException(
    val error: BusinessRulesErrors,
    val reason: String,
    val details: Map<String, String>? = null
) : Exception(reason) {

    /**
     * Create an easy-to-use constructor for a BusinessRuleException based on the data of a BindingResult
     */
    constructor(bindingResult: BindingResult, reason: String = "Validation error") :
            this(BusinessRulesErrors.Validation, reason, bindingResult.toMap())

}

enum class BusinessRulesErrors {
    InvalidEmail,
    InvalidPassword,
    NotFound,
    Validation,
}
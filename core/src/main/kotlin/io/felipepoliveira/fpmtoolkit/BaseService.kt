package io.felipepoliveira.fpmtoolkit

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.SmartValidator
import java.security.InvalidParameterException

abstract class BaseService(
    private val validator: SmartValidator
) {

    /**
     * Validate the given object returning a BindingResult containing the validation data
     */
    fun validate(obj: Any, modelValidationIdentifier: String = "object"): BindingResult {
        val bindingResult = BeanPropertyBindingResult(obj, modelValidationIdentifier)
        validator.validate(obj, bindingResult)
        return bindingResult
    }

    fun validatePagination(itemsPerPageToValidate: Int, pageToValidate: Int, maximumItemsPerPage: Int) {
        if (maximumItemsPerPage < 1) {
            throw InvalidParameterException("maximumItemsPerPage should be >= 1. $maximumItemsPerPage given")
        }

        if (itemsPerPageToValidate < 1 || itemsPerPageToValidate > maximumItemsPerPage) {
            throw BusinessRuleException(
                BusinessRulesError.INVALID_PARAMETERS,
                "itemsPerPage should be between 1 and $maximumItemsPerPage"
            )
        }
        if (pageToValidate < 1) {
            throw BusinessRuleException(
                BusinessRulesError.INVALID_PARAMETERS,
                "page should be greater or equals than 1"
            )
        }
    }
}
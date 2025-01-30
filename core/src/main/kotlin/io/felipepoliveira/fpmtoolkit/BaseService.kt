package io.felipepoliveira.fpmtoolkit

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.SmartValidator

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
}
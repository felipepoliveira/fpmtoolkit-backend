package io.felipepoliveira.fpmtoolkit.ext

import org.springframework.validation.BindingResult

/**
 * Parse a BindingResult into a map containing all the errors
 */
fun BindingResult.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (error in this.fieldErrors) {
        map[error.field] = error.defaultMessage ?: "Unknown error caught"
    }

    return map
}
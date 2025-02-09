package io.felipepoliveira.fpmtoolkit.features.users.dto

import jakarta.validation.constraints.NotBlank

data class ConfirmPrimaryEmailWithTokenDTO(
    /**
     * The confirmation token
     */
    @field:NotBlank
    val confirmationToken: String
)
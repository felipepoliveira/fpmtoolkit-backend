package io.felipepoliveira.fpmtoolkit.features.users.dto

import jakarta.validation.constraints.NotBlank

data class UpdatePrimaryEmailWithPrimaryEmailChangeTokenDTO(
    /**
     * The token used for the operation authentication
     */
    @field:NotBlank
    val token: String,

    /**
     * The verification code to change the primary email
     */
    @field:NotBlank
    val confirmationCode: String,
)

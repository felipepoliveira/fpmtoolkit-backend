package io.felipepoliveira.fpmtoolkit.features.users.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SendPrimaryEmailChangeMailDTO(
    /**
     * The new primary email that will be used by the account
     */
    @field:Email
    @field:NotBlank
    val newPrimaryEmail: String
)
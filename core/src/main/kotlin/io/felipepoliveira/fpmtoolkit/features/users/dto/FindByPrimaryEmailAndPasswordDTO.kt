package io.felipepoliveira.fpmtoolkit.features.users.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class FindByPrimaryEmailAndPasswordDTO(

    /**
     * The email used to find the user
     */
    @field:Email
    @field:NotBlank
    val primaryEmail: String,

    /**
     * The password of the user identified by the email
     */
    @field:NotBlank
    val password: String,
)
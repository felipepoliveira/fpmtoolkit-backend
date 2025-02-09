package io.felipepoliveira.fpmtoolkit.features.users.dto

import jakarta.validation.constraints.NotBlank

data class UpdatePasswordWithRecoveryTokenDTO(
    /**
     * The recovery token used to recover the password
     */
    @field:NotBlank
    val recoveryToken: String,

    /**
     * The new password that will be applied in the account
     */
    @field:NotBlank
    val newPassword: String,
)
package io.felipepoliveira.fpmtoolkit.features.users.dto

import jakarta.validation.constraints.NotBlank

data class UpdatePasswordDTO(
    /**
     * The current password of the user account
     */
    @field:NotBlank
    val currentPassword: String,

    /**
     * The new password of the user account
     */
    @field:NotBlank
    val newPassword: String,
)
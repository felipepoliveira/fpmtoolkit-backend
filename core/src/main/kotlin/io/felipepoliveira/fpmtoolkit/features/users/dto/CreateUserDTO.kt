package io.felipepoliveira.fpmtoolkit.features.users.dto

import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateUserDTO(
    /**
     * The user password
     */
    @field:NotBlank
    val password: String,

    /**
     * The preferred region of the user
     */
    @field:NotNull
    val preferredRegion: I18nRegion,

    /**
     * The presentation name of the user
     */
    @field:NotBlank
    val presentationName: String,

    /**
     * The user primary email
     */
    @field:NotNull
    @field:Email
    val primaryEmail: String,
)
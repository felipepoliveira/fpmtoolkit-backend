package io.felipepoliveira.fpmtoolkit.features.users.dto

import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class UpdateUserDTO(

    /**
     * The name used as presentation for the user
     */
    @field:NotBlank
    @field:Size(max = 60)
    val presentationName: String,

    /**
     * The user preferred region
     */
    @field:NotNull
    val preferredRegion: I18nRegion,
)
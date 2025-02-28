package io.felipepoliveira.fpmtoolkit.features.organizations.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class CreateOrganizationDTO(

    /**
     * The presentation name of the organization
     */
    @field:NotBlank
    @field:Size(max = 80)
    val presentationName: String,

    /**
     * The profile name (set by the user). Used to publicly identify the organization
     */
    profileName: String
) {

    /**
     * The profile name (set by the user). Used to publicly identify the organization
     */

    @field:NotBlank
    @field:Pattern(regexp = "^[a-zA-Z0-9_-]{1,30}\$")
    val profileName = profileName.lowercase()
}
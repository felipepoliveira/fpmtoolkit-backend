package io.felipepoliveira.fpmtoolkit.features.organizations.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateOrganizationDTO(
    /**
     * The presentation name of the organization
     */
    @field:NotBlank
    @field:Size(max = 80)
    val presentationName: String,
)
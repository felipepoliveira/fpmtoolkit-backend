package io.felipepoliveira.fpmtoolkit.features.projects.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateOrUpdateProjectDTO(
    /**
     * The profile name of the project
     */
    @field:NotBlank
    @field:Pattern(regexp = "^[\\w-]+\$")
    @field:Size(min = 1, max = 60)
    val profileName: String,

    /**
     * The name of the project
     */
    @field:NotBlank
    @field:Size(min = 1, max = 140)
    val name: String,
)
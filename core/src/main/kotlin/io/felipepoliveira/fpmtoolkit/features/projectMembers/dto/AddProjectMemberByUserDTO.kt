package io.felipepoliveira.fpmtoolkit.features.projectMembers.dto

import jakarta.validation.constraints.NotBlank

data class AddProjectMemberByUserDTO(
    /**
     * The user that will be added/update as a project member
     */
    @field:NotBlank
    val userUuid: String,
)
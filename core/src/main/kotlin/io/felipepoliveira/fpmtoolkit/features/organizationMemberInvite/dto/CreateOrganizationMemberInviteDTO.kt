package io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateOrganizationMemberInviteDTO(
    /**
     * The email of the user that will receive the invite
     */
    @field:Email
    @field:NotBlank
    val memberEmail: String,
)
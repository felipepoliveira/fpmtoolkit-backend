package io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.dto

import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
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

    /**
     * The region used in the invite language
     */
    @field:NotNull
    val inviteMailLanguage: I18nRegion,
)
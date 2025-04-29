package io.felipepoliveira.fpmtoolkit.api.controllers.dto

import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import jakarta.validation.constraints.NotNull

data class SendOrganizationMemberInviteMailDTO(
    /**
     * The language used in the mail
     */
    @field:NotNull
    val mailLanguage: I18nRegion
)
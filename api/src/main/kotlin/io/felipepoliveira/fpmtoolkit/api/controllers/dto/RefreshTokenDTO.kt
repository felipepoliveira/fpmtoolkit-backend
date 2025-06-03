package io.felipepoliveira.fpmtoolkit.api.controllers.dto

data class RefreshTokenDTO(
    /**
     * The given organization used to check the specific roles of the user in the organization context
     */
    val organizationId: String?,
)
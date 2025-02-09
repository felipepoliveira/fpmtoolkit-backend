package io.felipepoliveira.fpmtoolkit.api.controllers.dto

data class SendPasswordRecoveryMailDTO(
    /**
     * The primary email of the user
     */
    val primaryEmail: String
)
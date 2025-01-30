package io.felipepoliveira.fpmtoolkit.features.users

import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

class UserModel(
    /**
     * The hashed password from the user
     */
    @field:NotNull
    val hashedPassword: String,

    /**
     * The user ID used as primary key in the database
     */
    @field:NotNull
    val id: Long?,

    /**
     * The preferred region of the user
     */
    @field:NotNull
    val preferredRegion: I18nRegion,

    /**
     * The user primary email
     */
    @field:Email
    @field:NotNull
    val primaryEmail: String,

    /**
     * The name used as presentation for the user
     */
    @field:NotBlank
    @field:Size(max = 60)
    val presentationName: String,

    /**
     * The UUID of the user
     */
    @field:NotNull
    val uuid: UUID,
)
package io.felipepoliveira.fpmtoolkit.features.users

import com.fasterxml.jackson.annotation.JsonIgnore
import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(name = "user", indexes = [
    Index(name = "UI_primary_email_AT_user", columnList = "primary_email", unique = true),
    Index(name = "UI_uuid_AT_user", columnList = "uuid", unique = true),
])
class UserModel(
    /**
     * The hashed password from the user
     */
    @field:Column(name = "hashed_pwd", length = 64, nullable = false)
    @field:NotNull
    @field:JsonIgnore
    var hashedPassword: String,

    /**
     * The user ID used as primary key in the database
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:JsonIgnore
    val id: Long?,

    /**
     * The preferred region of the user
     */
    @field:Column(name = "preferred_region", length = 8, nullable = false)
    @field:Enumerated(EnumType.STRING)
    @field:NotNull
    var preferredRegion: I18nRegion,

    /**
     * The user primary email
     */
    @field:Column(name = "primary_email", length = 120, nullable = false)
    @field:Email
    @field:NotNull
    var primaryEmail: String,

    /**
     * When the primary email was confirmed by the user
     */
    @field:Column(name = "primary_email_confirmed_at")
    var primaryEmailConfirmedAt: LocalDateTime?,

    /**
     * The name used as presentation for the user
     */
    @field:Column(name = "presentation_name", length = 60, nullable = false)
    @field:NotBlank
    @field:Size(max = 60)
    var presentationName: String,

    /**
     * The UUID of the user
     */
    @field:Column(name = "uuid", length = 40, nullable = false)
    @field:NotNull
    val uuid: String,
)
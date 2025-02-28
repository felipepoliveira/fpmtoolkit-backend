package io.felipepoliveira.fpmtoolkit.features.organizations

import com.fasterxml.jackson.annotation.JsonIgnore
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(name = "organization", indexes = [
    Index(name = "UI_profile_name_AT_organization", columnList = "profile_name", unique = true),
    Index(name = "UI_uuid_AT_organization", columnList = "uuid", unique = true)
])
class OrganizationModel(

    /**
     * When the organization was created
     */
    @field:Column(name = "created_at", nullable = false)
    @field:JsonIgnore
    val createdAt: LocalDateTime,

    /**
     * The ID of the organization
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:JsonIgnore
    val id: Long?,

    /**
     * The owner of the organization
     */
    @field:JsonIgnore
    @field:JoinColumn(name = "owner_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY, optional = false)
    val owner: UserModel,

    /**
     * The presentation name of the organization
     */
    @field:Column(name = "presentation_name", length = 60, nullable = false)
    @field:NotBlank
    @field:Size(max = 80)
    val presentationName: String,

    /**
     * The profile name (set by the user). Used to publicly identify the organization
     */
    @field:Column(name = "profile_name", length = 30, nullable = false)
    @field:NotBlank
    @field:Pattern(regexp = "^[a-zA-Z0-9_-]{1,30}\$")
    val profileName: String,

    /**
     * The UUID of the organization
     */
    @field:Column(name = "uuid", length = 40, nullable = false)
    @field:NotNull
    val uuid: String,
)
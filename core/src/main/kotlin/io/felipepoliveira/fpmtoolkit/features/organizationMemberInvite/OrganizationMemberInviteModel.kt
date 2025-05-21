package io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite

import com.fasterxml.jackson.annotation.JsonIgnore
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "organization_member_invite", indexes = [
    // it is not possible too duplicate the email in the same organization
    Index(
        name = "UI_member_email_AND_organization_id_AT_organization_member_invite",
        columnList = "member_email, organization_id", unique = true
    )
])
class OrganizationMemberInviteModel(
    /**
     * The ID used in the database
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:JsonIgnore
    val id: Long?,

    /**
     * When the invite was created
     */
    @field:Column(name = "created_at", nullable = false)
    @field:Temporal(value = TemporalType.TIMESTAMP)
    val createdAt: LocalDateTime,

    /**
     * The email that will receive the invite
     */
    @field:Email
    @field:Column(name = "member_email", nullable = false, length = 120)
    val memberEmail: String,

    /**
     * The organization that owns the invite
     */
    @field:ManyToOne
    @field:JoinColumn(name = "organization_id", nullable = false)
    @field:NotNull
    val organization: OrganizationModel,

    /**
     * The UUID used to identify the invite
     */
    @field:Column(name = "uuid", nullable = false, length = 40)
    @field:NotBlank
    val uuid: String,
)
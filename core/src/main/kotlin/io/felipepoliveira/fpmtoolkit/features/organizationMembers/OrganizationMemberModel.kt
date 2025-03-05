package io.felipepoliveira.fpmtoolkit.features.organizationMembers

import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "organization_member", indexes = [
    Index(name = "UI_organization_id_AND_user_id_AT_organization_member", columnList = "organization_id, user_id", unique = true)
])
class OrganizationMemberModel(
    /**
     * The ID used in the database
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    /**
     * Flag that indicates if the member is the owner of the organization. Only one owner could exist in an organization
     */
    @field:Column(name = "is_owner", nullable = false)
    val isOrganizationOwner: Boolean,

    /**
     * The user associated in the organization membership
     */
    @field:ManyToOne
    @field:JoinColumn(name = "organization_id", nullable = false)
    @field:NotNull
    val organization: OrganizationModel,

    /**
     * The roles applied into the member
     */
    @field:ElementCollection(targetClass = OrganizationMemberRoles::class)
    @field:JoinTable(
        name = "organization_member_roles",
        joinColumns = [JoinColumn(name = "organization_member_id")]
    ) @Column(
        name = "role",
        nullable = false
    ) @Enumerated(EnumType.STRING)
    val roles: Collection<OrganizationMemberRoles>,

    /**
     * The user associated in the organization membership
     */
    @field:ManyToOne
    @field:JoinColumn(name = "user_id", nullable = false)
    @field:NotNull
    val user: UserModel,
) {


}

/**
 * Store the roles that an organization member can have
 */
enum class OrganizationMemberRoles {
    /**
     * Mark the member as the administrator
     */
    ORG_ADMINISTRATOR
}
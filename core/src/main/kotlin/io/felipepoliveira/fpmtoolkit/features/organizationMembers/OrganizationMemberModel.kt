package io.felipepoliveira.fpmtoolkit.features.organizationMembers

import com.fasterxml.jackson.annotation.JsonIgnore
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "organization_member", indexes = [
    Index(name = "UI_organization_id_AND_user_id_AT_organization_member", columnList = "organization_id, user_id", unique = true),
    Index(name = "UI_uuid_AT_organization_member", columnList = "uuid", unique = true),
])
class OrganizationMemberModel(
    /**
     * The ID used in the database
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:JsonIgnore
    val id: Long?,

    /**
     * The UUID used to identify the member
     */
    @field:Column(name = "uuid", nullable = false, length = 40)
    @field:NotBlank
    val uuid: String,

    /**
     * Flag that indicates if the member is the owner of the organization. Only one owner could exist in an organization
     */
    @field:Column(name = "is_owner", nullable = false)
    var isOrganizationOwner: Boolean,

    /**
     * The user associated in the organization membership
     */
    @field:ManyToOne
    @field:JoinColumn(name = "organization_id", nullable = false)
    @field:NotNull
    @field:JsonIgnore
    val organization: OrganizationModel,

    /**
     * The roles applied into the member
     */
    @field:ElementCollection(targetClass = OrganizationMemberRoles::class, fetch = FetchType.EAGER)
    @field:JoinTable(
        name = "organization_member_roles",
        joinColumns = [JoinColumn(name = "organization_member_id")],
    ) @Column(
        name = "role",
        nullable = false,
        length = 60
    ) @Enumerated(EnumType.STRING)
    var roles: Collection<OrganizationMemberRoles>,

    /**
     * The user associated in the organization membership
     */
    @field:ManyToOne
    @field:JoinColumn(name = "user_id", nullable = false)
    @field:NotNull
    val user: UserModel,
) {

    fun assertIsOwnerOr(vararg roles: OrganizationMemberRoles): OrganizationMemberModel {
        if (!isOwnerOr(*roles)) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "User does not have the required role to the requested service"
            )
        }

        return this
    }

    fun assertIsOwnerOrOrganizationAdministratorOr(vararg roles: OrganizationMemberRoles): OrganizationMemberModel {
        val extendedRoles = roles.toList() + OrganizationMemberRoles.ORG_ADMINISTRATOR
        return assertIsOwnerOr(*extendedRoles.toTypedArray())

    }

    /**
     * Return a flag indicating if this member is from the same organization as the given one
     */
    fun isFromSameOrganization(another: OrganizationMemberModel) = this.organization.id == another.organization.id


    /**
     * Return a flag if this member is the owner of the organization or contains any of the given roles
     */
    fun isOwnerOr(vararg roles: OrganizationMemberRoles): Boolean {
        // return true if the user is the owner of the organization
        if (this.isOrganizationOwner) {
            return true
        }

        // return a flag if any of the given roles is contained in the organization member roles collection
        return roles.any { r -> this.roles.contains(r) }
    }

    /**
     * Return a flag indicating if the member is owner, ORG_ADMINISTRATOR or has another given 'roles'
     */
    fun isOwnerOrAdministratorOr(vararg roles: OrganizationMemberRoles): Boolean {
        val extendedRoles = roles.toList() + OrganizationMemberRoles.ORG_ADMINISTRATOR
        return isOwnerOr(*extendedRoles.toTypedArray())
    }

}

/**
 * Store the roles that an organization member can have
 */
enum class OrganizationMemberRoles {
    /**
     * Organization administrator que do everything to manage the organization. The only restrictions are:
     * - They cannot delete the organization
     * - They cannot change the owner of the organization (only the owner can do that)
     */
    ORG_ADMINISTRATOR,

    /**
     * They can manage all members of an organization. That includes:
     * - Manage organization members and invites (Add, update roles and remove)
     */
    ORG_MEMBER_ADMINISTRATOR,

    /**
     * They can manage the projects of the organization. That includes:
     * - Create a new project
     */
    ORG_PROJECT_ADMINISTRATOR,

}
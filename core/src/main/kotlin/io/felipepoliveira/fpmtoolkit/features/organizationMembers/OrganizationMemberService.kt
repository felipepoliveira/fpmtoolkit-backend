package io.felipepoliveira.fpmtoolkit.features.organizationMembers

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrganizationMemberService @Autowired constructor(
    private val organizationMemberDAO: OrganizationMemberDAO,
    private val userService: UserService,
) {

    companion object {
        const val MAXIMUM_AMOUNT_OF_FREE_MEMBERS: Int = 20
    }

    /**
     * Add a new organization member in the given organization
     */
    @Transactional
    internal fun addOrganizationMember(
        organization: OrganizationModel,
        newMember: OrganizationMemberModel): OrganizationMemberModel {

        // if the user is already a member of the organization throw an error
        if (organizationMemberDAO.findByOrganizationAndUser(organization, newMember.user) != null) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "This user is already a member of the organization"
            )
        }

        // check if the maximum amount of members per organization has being reached
        val paginationOfOrgMembers = organizationMemberDAO.paginationByOrganization(organization, 1)
        if (paginationOfOrgMembers.totalRecords >= MAXIMUM_AMOUNT_OF_FREE_MEMBERS) {
            throw BusinessRuleException(
                BusinessRulesError.PAYMENT_REQUIRED,
                "The maximum amount of members in this organization has being reach. " +
                        "Current amount: ${paginationOfOrgMembers.totalRecords}; Limit: $MAXIMUM_AMOUNT_OF_FREE_MEMBERS"
            )
        }

        // if the new member is marked to be the owner of the organization and organization already have an owner
        // throw an error
        val currentOrgOwner = organizationMemberDAO.findOwnerByOrganization(organization)
        if (newMember.isOrganizationOwner && currentOrgOwner != null) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "The new member is marked to be the organization owner but the organization already have a owner"
            )
        }

        // persist the new member into the database
        organization.members.add(newMember)
        organizationMemberDAO.persist(newMember)

        return newMember
    }

    /**
     * Find an organization member identified by the given organization and the user account associated
     * with the OrganizationMemberModel
     */
    fun findByOrganizationAndUserOrForbidden(organization: OrganizationModel, user: UserModel): OrganizationMemberModel {
        return organizationMemberDAO.findByOrganizationAndUser(organization, user) ?: throw BusinessRuleException(
            BusinessRulesError.FORBIDDEN,
            "Could not found membership of user ${user.uuid} in organization ${organization.profileName}"
        )
    }

    /**
     * Remove a member from the organization. The applied rules are:
     * - The user can only be deleted if the requester is the owner or administrator of the target member organization
     * - The member can only be deleted if it is not the owner of the organization
     */
    fun removeOrganizationMember(requesterUuid: String, targetOrganizationUuid: String, targetMemberUuid: String): OrganizationMemberModel {
        // fetch the requester account
        val requester = userService.assertFindByUuid(requesterUuid)

        // fetch the target member to remove
        val targetMemberToRemove = organizationMemberDAO.findByUuid(targetMemberUuid) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "Can not find a member identified by UUID: $targetMemberUuid"
        )

        // the requester can only delete the target member if it has the required role or is the owner
        findByOrganizationAndUserOrForbidden(targetMemberToRemove.organization, requester)
            .assertIsOwnerOr(OrganizationMemberRoles.ORG_ADMINISTRATOR)

        return removeOrganizationMember(targetMemberToRemove)

    }

    /**
     * Remove a member from the organization. The applied rules are:
     * - The member can only be deleted if it is not the owner of the organization
     */
    @Transactional
    private fun removeOrganizationMember(targetMemberToRemove: OrganizationMemberModel): OrganizationMemberModel {

        // the owner of the organization can not be removed
        if (targetMemberToRemove.isOrganizationOwner) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "The owner of the organization can not be removed"
            )
        }

        organizationMemberDAO.delete(targetMemberToRemove)
        return targetMemberToRemove
    }

}
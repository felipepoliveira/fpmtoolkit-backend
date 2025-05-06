package io.felipepoliveira.fpmtoolkit.features.organizationMembers

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteDAO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationDAO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.users.UserDAO
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.security.tokens.OrganizationMemberInviteTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class OrganizationMemberService @Autowired constructor(
    private val organizationMemberDAO: OrganizationMemberDAO,
    private val organizationMemberInviteTokenProvider: OrganizationMemberInviteTokenProvider,
    private val organizationMemberInviteDAO: OrganizationMemberInviteDAO,
    private val organizationDAO: OrganizationDAO,
    private val userDAO: UserDAO,
    private val userService: UserService,
) {

    companion object {
        const val MAXIMUM_AMOUNT_OF_FREE_MEMBERS: Int = 20
        const val PAGINATION_LIMIT: Int = 20
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
     * Return data about the organization members of the given organization. Also, this method
     * will verify if the given requester is a member of the organization
     */
    fun findByOrganization(organization: OrganizationModel, requester: UserModel,
                           itemsPerPage: Int, page: Int): Collection<OrganizationMemberModel> {
        //TODO implement unit test
        val requesterMembership = findByOrganizationAndUserOrForbidden(organization, requester)
        return organizationMemberDAO.findByOrganization(
            organization,
            itemsPerPage.coerceIn(1..PAGINATION_LIMIT),
            page.coerceAtLeast(1)
        )
    }

    /**
     * Return pagination metadata about the organization members of the given organization. Also, this method
     * will verify if the given requester is a member of the organization
     */
    fun paginationByOrganization(organization: OrganizationModel, requester: UserModel, itemsPerPage: Int): Pagination {
        //TODO implement unit test
        val requesterMembership = findByOrganizationAndUserOrForbidden(organization, requester)
        return organizationMemberDAO.paginationByOrganization(organization, itemsPerPage.coerceIn(1..PAGINATION_LIMIT))
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
     * Add a new organization member using invite
     */
    fun ingressByInvite(token: String): OrganizationMemberModel {

        // Decode the given token
        val decodedToken = organizationMemberInviteTokenProvider.validateAndDecode(token)
            ?: throw BusinessRuleException(
                error = BusinessRulesError.INVALID_CREDENTIALS,
                reason = "Invalid token"
            )

        // try to find the invite from the database
        val invite = organizationMemberInviteDAO.findByUuid(decodedToken.inviteId) ?: throw BusinessRuleException(
            error = BusinessRulesError.FORBIDDEN,
            reason = "Could not find the invite identified by '${decodedToken.inviteId}'"
        )

        // assert that the user has an account in the platform
        val recipientUserAccount = userDAO.findByPrimaryEmail(invite.memberEmail) ?: throw BusinessRuleException(
            error = BusinessRulesError.FORBIDDEN,
            reason = "The user identified by the email '${invite.memberEmail}' should have an account before joining the organization"
        )

        // add the new member into organization
        val organizationMember = OrganizationMemberModel(
            id = null,
            organization = invite.organization,
            isOrganizationOwner = false,
            uuid = UUID.randomUUID().toString(),
            user = recipientUserAccount,
            roles = emptyList()
        )

        // add the organization member in the database
        organizationMemberDAO.persist(organizationMember)

        return organizationMember
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
package io.felipepoliveira.fpmtoolkit.features.organizationMembers

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteDAO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationDAO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserDAO
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.security.tokens.OrganizationMemberInviteTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.SmartValidator
import java.util.*

@Service
class OrganizationMemberService @Autowired constructor(
    private val organizationMemberDAO: OrganizationMemberDAO,
    private val organizationMemberInviteTokenProvider: OrganizationMemberInviteTokenProvider,
    private val organizationMemberInviteDAO: OrganizationMemberInviteDAO,
    private val organizationDAO: OrganizationDAO,
    private val userDAO: UserDAO,
    private val userService: UserService,
    smartValidator: SmartValidator
) : BaseService(smartValidator) {

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
        val paginationOfOrgMembers = organizationMemberDAO.paginationByOrganization(organization, null, 1)
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
     * Set a new organization owner. Only the organization owner can make this operation
     */
    @Transactional
    fun changeOrganizationOwner(requesterUuid: String, targetMemberUuid: String): OrganizationMemberModel {
        val requesterUser = userService.assertFindByUuid(requesterUuid)
        val targetMember = findByUuid(targetMemberUuid)
        val requesterMembership = findByOrganizationAndUserOrForbidden(targetMember.organization, requesterUser)

        // Only the organization owner can do that
        if (!requesterMembership.isOrganizationOwner) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "Only the organization owner can make this operation"
            )
        }

        // The organization owner can not set itself as owner
        if (requesterMembership.id == targetMember.id) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "You are currently the owner of the organization"
            )
        }

        // update the models on the database,
        // the requester will always receive 'ORG_ADMINISTRATOR' privileges after the update
        requesterMembership.isOrganizationOwner = false
        requesterMembership.roles = mutableListOf(OrganizationMemberRoles.ORG_ADMINISTRATOR)
        targetMember.isOrganizationOwner = true
        organizationMemberDAO.update(requesterMembership)
        organizationMemberDAO.update(targetMember)

        return targetMember
    }

    /**
     * Return data about the organization members of the given organization. Also, this method
     * will verify if the given requester is a member of the organization
     */
    fun findByOrganization(organization: OrganizationModel, requester: UserModel,
                           itemsPerPage: Int, page: Int,
                           queryField: String?): Collection<OrganizationMemberModel> {
        // assert that requester is from the same organization
        findByOrganizationAndUserOrForbidden(organization, requester)


        return organizationMemberDAO.findByOrganization(
            organization,
            queryField,
            itemsPerPage.coerceIn(1..PAGINATION_LIMIT),
            page.coerceAtLeast(1)
        )
    }

    /**
     * Return pagination metadata about the organization members of the given organization. Also, this method
     * will verify if the given requester is a member of the organization
     */
    fun paginationByOrganization(organization: OrganizationModel, requester: UserModel, itemsPerPage: Int, queryField: String?): Pagination {
        // Assert that requester is from the same organization
        findByOrganizationAndUserOrForbidden(organization, requester)


        return organizationMemberDAO.paginationByOrganization(
            organization, queryField, itemsPerPage.coerceIn(1..PAGINATION_LIMIT)
        )
    }

    fun findByOrganizationAndUser(organization: OrganizationModel, user: UserModel): OrganizationMemberModel {
        return organizationMemberDAO.findByOrganizationAndUser(organization, user) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "Could not found membership of user ${user.uuid} in organization ${organization.profileName}"
        )
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
     * Return the OrganizationMemberModel UUID
     */
    fun findByUuid(memberUuid: String): OrganizationMemberModel {
        return organizationMemberDAO.findByUuid(memberUuid) ?: throw BusinessRuleException(
            error = BusinessRulesError.NOT_FOUND,
            reason =  "Could not find organization member identified by uuid '$memberUuid'"
        )
    }

    /**
     * Remove a member from the organization. The applied rules are:
     * - The user can only be deleted if the requester is the owner or administrator of the target member organization
     * - The member can only be deleted if it is not the owner of the organization
     */
    @Transactional
    fun removeOrganizationMember(requesterUuid: String, targetOrganizationUuid: String, targetMemberUuid: String): OrganizationMemberModel {
        // fetch the requester account
        val requester = userService.assertFindByUuid(requesterUuid)

        // fetch the target member to remove
        val targetMemberToRemove = organizationMemberDAO.findByUuid(targetMemberUuid) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "Can not find a member identified by UUID: $targetMemberUuid"
        )

        // the requester can only delete the target member if it has the required role or is the owner
        val requesterMembership = findByOrganizationAndUserOrForbidden(targetMemberToRemove.organization, requester)
            .assertIsOwnerOr(OrganizationMemberRoles.ORG_ADMINISTRATOR)

        // check if the target user is from the same organization as the requester membership in the given organization
        if (targetMemberToRemove.organization.id != requesterMembership.organization.id) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "You can not remove a member of another organization"
            )
        }

        return removeOrganizationMember(targetMemberToRemove)
    }

    /**
     * Add a new organization member using invite
     */
    @Transactional
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

        // assert that the user is not a member yet
        if (organizationMemberDAO.findByOrganizationAndUser(invite.organization, recipientUserAccount) != null) {
            throw BusinessRuleException(
                error = BusinessRulesError.DUPLICATED,
                reason = "The user is already a member of the organization"
            )
        }

        // add the new member into organization
        val organizationMember = OrganizationMemberModel(
            id = null,
            organization = invite.organization,
            isOrganizationOwner = false,
            uuid = UUID.randomUUID().toString(),
            user = recipientUserAccount,
            roles = emptyList()
        )

        // add the organization member in the database and delete the invite
        organizationMemberDAO.persist(organizationMember)
        organizationMemberInviteDAO.delete(invite)

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

    /**
     *
     */
    @Transactional
    fun update(requesterUuid: String, targetMemberUuid: String, dto: UpdateOrganizationMemberDTO): OrganizationMemberModel {

        // check for validation results
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // fetch the requester account
        val requester = userService.assertFindByUuid(requesterUuid)

        // assert target user exists
        val targetMember = findByUuid(targetMemberUuid)

        // assert that the requester has the required role for the operation
        findByOrganizationAndUserOrForbidden(targetMember.organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_MEMBER_ADMINISTRATOR)

        // update the target member in the database
        targetMember.roles = dto.roles
        organizationMemberDAO.update(targetMember)

        return targetMember
    }

}
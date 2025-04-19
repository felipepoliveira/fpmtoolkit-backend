package io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.dto.CreateOrganizationMemberInviteDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.SmartValidator
import java.time.LocalDateTime
import java.util.*

@Service
class OrganizationMemberInviteService @Autowired constructor(
    private val organizationMemberInviteDAO: OrganizationMemberInviteDAO,
    private val organizationService: OrganizationService,
    private val organizationMemberDAO: OrganizationMemberDAO,
    private val organizationMemberService: OrganizationMemberService,
    private val userService: UserService,
    private val validator: SmartValidator,
) : BaseService(validator) {

    companion object {
        /**
         * Store the maximum amount of pending invites per organization
         */
        const val LIMIT_OF_INVITES_PER_ORGANIZATION = 100
        const val PAGINATION_LIMIT = 20
    }
    @Transactional
    fun create(requesterUuid: String, organizationUuid: String, dto: CreateOrganizationMemberInviteDTO): OrganizationMemberInviteModel {

        // check for DTO validation errors
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // fetch data from database
        val requester = userService.assertFindByUuid(requesterUuid)
        val organization = organizationService.findByUuid(organizationUuid)

        // check for requester authorization
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_MEMBER_ADMINISTRATOR)

        // check if there is already a member or invite with the given email
        if (organizationMemberDAO.findByOrganizationAndUserPrimaryEmail(organization, dto.memberEmail) != null ||
            organizationMemberInviteDAO.findByOrganizationAndMemberEmail(organization, dto.memberEmail) != null) {
            throw BusinessRuleException(
                reason = "There is already a member with the given email",
                error = BusinessRulesError.INVALID_EMAIL
            )
        }

        // check for invite limit
        val paginationOfInvites = organizationMemberInviteDAO.paginationByOrganization(organization, 1)
        if (paginationOfInvites.totalRecords >= LIMIT_OF_INVITES_PER_ORGANIZATION) {
            throw BusinessRuleException(
                reason = "Maximum amount of pending invites limit reached",
                error = BusinessRulesError.PAYMENT_REQUIRED
            )
        }

        //TODO send the invite email

        // create a new invite and persist it on the database
        val invite = OrganizationMemberInviteModel(
            id = null,
            organization = organization,
            createdAt = LocalDateTime.now(),
            uuid = UUID.randomUUID().toString(),
            memberEmail = dto.memberEmail
        )
        organizationMemberInviteDAO.persist(invite)

        return invite
    }

    fun findByOrganization(organizationUuid: String, limit: Int, page: Int): Collection<OrganizationMemberInviteModel> {
        return organizationMemberInviteDAO.findByOrganization(
            organizationService.findByUuid(organizationUuid),
            limit.coerceIn(1..PAGINATION_LIMIT),
            page.coerceAtLeast(1)
        )
    }

    fun paginationByOrganization(organizationUuid: String, limit: Int): Pagination {
        return organizationMemberInviteDAO.paginationByOrganization(
            organizationService.findByUuid(organizationUuid),
            limit.coerceIn(1..PAGINATION_LIMIT),
        )
    }

    fun findByUuid(inviteUuid: String): OrganizationMemberInviteModel {
        return organizationMemberInviteDAO.findByUuid(inviteUuid) ?: throw BusinessRuleException(
            reason = "Could not find invite identified by uuid: $inviteUuid",
            error = BusinessRulesError.NOT_FOUND
        )
    }

    /**
     * Remove a pending invite from the database
     */
    @Transactional
    fun remove(requesterUuid: String, inviteUuid: String): OrganizationMemberInviteModel {
        // fetch data from database
        val requester = userService.assertFindByUuid(requesterUuid)
        val invite = findByUuid(inviteUuid)

        // check for requester authorization
        organizationMemberService.findByOrganizationAndUserOrForbidden(invite.organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_MEMBER_ADMINISTRATOR)

        // delete the invite from the database
        organizationMemberInviteDAO.delete(invite)

        return invite

    }

}
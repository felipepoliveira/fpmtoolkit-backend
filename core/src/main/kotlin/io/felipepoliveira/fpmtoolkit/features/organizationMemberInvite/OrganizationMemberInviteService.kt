package io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.dto.CreateOrganizationMemberInviteDTO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationMail
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteMail
import io.felipepoliveira.fpmtoolkit.security.tokens.OrganizationMemberInviteTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.SmartValidator
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class OrganizationMemberInviteService @Autowired constructor(
    private val contextualBeans: ContextualBeans,
    private val organizationMemberInviteCache: OrganizationMemberInviteCache,
    private val organizationMemberInviteMail: OrganizationMemberInviteMail,
    private val organizationInviteTokenProvider: OrganizationMemberInviteTokenProvider,
    private val organizationMemberDAO: OrganizationMemberDAO,
    private val organizationMemberInviteDAO: OrganizationMemberInviteDAO,
    private val organizationMemberService: OrganizationMemberService,
    private val organizationService: OrganizationService,
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
        val paginationOfInvites = organizationMemberInviteDAO.paginationByOrganization(organization, 1, null)
        if (paginationOfInvites.totalRecords >= LIMIT_OF_INVITES_PER_ORGANIZATION) {
            throw BusinessRuleException(
                reason = "Maximum amount of pending invites limit reached",
                error = BusinessRulesError.PAYMENT_REQUIRED
            )
        }

        // create a new invite and persist it on the database
        val invite = OrganizationMemberInviteModel(
            id = null,
            organization = organization,
            createdAt = LocalDateTime.now(),
            uuid = UUID.randomUUID().toString(),
            memberEmail = dto.memberEmail
        )
        organizationMemberInviteDAO.persist(invite)

        sendInviteMail(invite, dto.inviteMailLanguage)

        return invite
    }

    fun findByInviteToken(inviteToken: String): OrganizationMemberInviteModel {
        // decode the invite token
        val decodedInviteToken = organizationInviteTokenProvider.validateAndDecode(inviteToken) ?: throw
                BusinessRuleException(
                    error = BusinessRulesError.INVALID_CREDENTIALS,
                    reason = "Invalid token provided"
                )

        // fetch the token using its ID
        return findByUuid(decodedInviteToken.inviteId)
    }

    fun findByOrganization(requesterUuid: String, organizationUuid: String, limit: Int, page: Int, queryField: String?): Collection<OrganizationMemberInviteModel> {
        // assert requester has authorization to fetch data
        val requester = userService.assertFindByUuid(requesterUuid)
        val organization = organizationService.findByUuid(organizationUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_MEMBER_ADMINISTRATOR)


        return organizationMemberInviteDAO.findByOrganization(
            organizationService.findByUuid(organizationUuid),
            limit.coerceIn(1..PAGINATION_LIMIT),
            page.coerceAtLeast(1),
            queryField
        )
    }

    fun paginationByOrganization(requesterUuid: String, organizationUuid: String, limit: Int, queryField: String?): Pagination {
        // assert requester has authorization to fetch data
        val requester = userService.assertFindByUuid(requesterUuid)
        val organization = organizationService.findByUuid(organizationUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_MEMBER_ADMINISTRATOR)

        return organizationMemberInviteDAO.paginationByOrganization(
            organizationService.findByUuid(organizationUuid),
            limit.coerceIn(1..PAGINATION_LIMIT),
            queryField
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

    fun sendInviteMail(requesterUuid: String, inviteUuid: String, language: I18nRegion) {
        // assert that the user is
        val requester = userService.assertFindByUuid(requesterUuid)
        val invite = findByUuid(inviteUuid)
        organizationMemberService
            .findByOrganizationAndUserOrForbidden(invite.organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_MEMBER_ADMINISTRATOR)

        sendInviteMail(invite, language)

    }

    fun sendInviteMail(invite: OrganizationMemberInviteModel, language: I18nRegion) {
        // Using cache timeout, issue the invite token and sent it via email
        organizationMemberInviteCache.executeOnOrganizationMemberInviteMailTimeout(invite.organization, invite.memberEmail) {
            // issuing the token
            val inviteTokenAndPayload = organizationInviteTokenProvider.issue(
                invite = invite,
                recipientEmail = invite.memberEmail,
                expiresAt = Instant.now().plus(7, ChronoUnit.DAYS)
            )
            // building the invite URL
            val inviteUrl = "${contextualBeans.webappUrl()}/join-organization?token=${inviteTokenAndPayload.token}"
            // send the mail
            organizationMemberInviteMail.sendInviteMail(
                language,
                invite.organization,
                invite.memberEmail,
                inviteUrl
            )
        }
    }

}
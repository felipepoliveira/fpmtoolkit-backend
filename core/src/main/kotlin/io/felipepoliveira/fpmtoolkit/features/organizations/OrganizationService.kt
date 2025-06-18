package io.felipepoliveira.fpmtoolkit.features.organizations

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.ext.addError
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.dto.CreateOrganizationDTO
import io.felipepoliveira.fpmtoolkit.features.organizations.dto.UpdateOrganizationDTO
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.SmartValidator
import java.time.LocalDateTime
import java.util.*

@Service
class OrganizationService @Autowired constructor(
    private val organizationDAO: OrganizationDAO,
    private val organizationMemberService: OrganizationMemberService,
    private val userService: UserService,
    private val validator: SmartValidator
) : BaseService(validator) {

    companion object {
        const val MAXIMUM_AMOUNT_OF_ORGANIZATIONS_PER_FREE_ACCOUNT: Int = 5
        const val PAGINATION_LIMIT = 20
    }

    /**
     * Return an OrganizationModel  identified by the given UUID, if the organization does not exist it throws NOT_FOUND.
     * Also, this method check if the given user is a member of the organization
     */
    fun findByUuid(uuid: String): OrganizationModel {
        // assert that organization exists by its UUID
        val organization = organizationDAO.findByUuid(uuid) ?:
            throw BusinessRuleException(
                BusinessRulesError.NOT_FOUND,
                "Organization identified by UUID $uuid not found"
            )

        return organization
    }

    /**
     * Return an organization identified by the given 'profileName' and check if the user identified by
     * 'userUuid' is a member of the organization
     */
    fun findByProfileNameAndCheckIfUserIsAMember(userUuid: String, profileName: String): OrganizationModel {
        val requester = userService.assertFindByUuid(userUuid)
        val organization = organizationDAO.findByProfileName(profileName) ?: throw BusinessRuleException(
            error = BusinessRulesError.NOT_FOUND,
            reason = "Could not find a organization with profileName '${profileName}'"
        )
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)

        //TODO implement unit tests for this method

        return organization
    }

    /**
     * Return an organization identified by the given `organizationUuid` and check if the user identified by
     * `userUuid` is a member of the organization
     */
    fun findByUuidAndCheckIfUserIsAMember(userUuid: String, organizationUuid: String): OrganizationModel {
        return findByUuidAndCheckIfUserIsAMember(userService.assertFindByUuid(userUuid), organizationUuid)
    }

    /**
     * Return an organization identified by the given `organizationUuid` and check if the user identified by
     * `userUuid` is a member of the organization
     */
    fun findByUuidAndCheckIfUserIsAMember(requester: UserModel, organizationUuid: String): OrganizationModel {
        val organization = findByUuid(organizationUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)

        return organization
    }

    /**
     * Create a new organization account into the platform
     */
    @Transactional
    fun createOrganization(requesterUuid: String, dto: CreateOrganizationDTO): OrganizationModel {
        // validate the DTO
        val validationResult = validate(dto)

        // check for specific rules for profile name
        if (!isOrganizationProfileNameValid(dto.profileName)) {
            validationResult.addError("profileName", "Profile name is invalid")
        }

        // check if the given name is already in use
        if (organizationDAO.findByProfileName(dto.profileName) != null) {
            validationResult.addError("profileName", "Profile name is not available")
        }

        // throw an exception if validationResult has any errors
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // get the requester account
        val requester = userService.assertFindByUuid(requesterUuid)

        // A requester can have 5 organizations
        val paginationOfOrganizationsFromRequester = organizationDAO.paginationByOwner(requester, 1)
        if (paginationOfOrganizationsFromRequester.totalRecords >= MAXIMUM_AMOUNT_OF_ORGANIZATIONS_PER_FREE_ACCOUNT) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "You reach the maximum amount of organizations owned by your account"
            )
        }

        // create the new organization model and persist on the database
        val organization = OrganizationModel(
            createdAt = LocalDateTime.now(),
            presentationName = dto.presentationName,
            id = null,
            members = mutableListOf(),
            profileName = dto.profileName,
            uuid = UUID.randomUUID().toString(),
        )
        organizationDAO.persist(organization)

        // add the user as member and owner of the organization
        val newOrgOwner = OrganizationMemberModel(
            id = null,
            user = requester,
            roles = listOf(),
            isOrganizationOwner = true,
            organization = organization,
            uuid = UUID.randomUUID().toString()
        )
        organizationMemberService.addOrganizationMember(organization, newOrgOwner)

        return organization
    }

    /**
     * Return all organizations where the given user is a member
     */
    fun findOrganizationsByMember(requesterUuid: String, itemsPerPage: Int, page: Int): Collection<OrganizationModel> {
        return findOrganizationsByMember(userService.assertFindByUuid(requesterUuid), itemsPerPage, page)
    }

    /**
     * Return all organizations where the given user is a member
     */
    fun findOrganizationsByMember(user: UserModel, itemsPerPage: Int, page: Int): Collection<OrganizationModel> {
        validatePagination(itemsPerPage, page, PAGINATION_LIMIT)
        return organizationDAO.findByMember(user, itemsPerPage, page)
    }

    /**
     * Return a flag indicating if the given profile name is available
     */
    fun isProfileNameAvailable(profileName: String): Boolean {
        return organizationDAO.findByProfileName(profileName) != null
    }

    /**
     * Return a flag indicating if the profile name of the organization is valid
     */
    private fun isOrganizationProfileNameValid(orgProfileName: String): Boolean {
        // The Free Prefix regex assert that the orgProfileName always ends with *"-<9 random letters and digits>"
        // this will assert that cool profile name will be available only for premium users
        val freePrefixRegex = Regex("(-[a-z0-9]{9})\$")
        val endsWithFreePrefix = freePrefixRegex.containsMatchIn(orgProfileName)
        return endsWithFreePrefix
    }

    /**
     * Update the organization identified by `targetOrganizationUuid`
     */
    fun updateOrganization(requesterUuid: String, targetOrganizationUuid: String, dto: UpdateOrganizationDTO): OrganizationModel {
        // check for validation result
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // fetch the user account
        val requester = userService.assertFindByUuid(requesterUuid)

        // fetch organization, check if requester is a member, and has the specific role
        val targetOrganization = findByUuid(targetOrganizationUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(
            targetOrganization, requester
        ).assertIsOwnerOr(OrganizationMemberRoles.ORG_ADMINISTRATOR)

        // update the organization
        targetOrganization.presentationName = dto.presentationName

        organizationDAO.update(targetOrganization)

        return targetOrganization
    }

}
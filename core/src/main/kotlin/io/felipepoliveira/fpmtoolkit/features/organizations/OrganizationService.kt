package io.felipepoliveira.fpmtoolkit.features.organizations

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.ext.addError
import io.felipepoliveira.fpmtoolkit.features.organizations.dto.CreateOrganizationDTO
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
    private val userService: UserService,
    private val validator: SmartValidator
) : BaseService(validator) {

    companion object {
        const val MAXIMUM_AMOUNT_OF_ORGANIZATIONS_PER_FREE_ACCOUNT: Int = 5
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
            uuid = UUID.randomUUID().toString(),
            owner = requester,
            profileName = dto.profileName
        )
        organizationDAO.persist(organization)

        return organization
    }

    private fun isOrganizationProfileNameValid(orgProfileName: String): Boolean {
        // The Free Prefix regex assert that the orgProfileName always ends with *"-<9 random letters and digits>"
        // this will assert that cool profile name will be available only for premium users
        val freePrefixRegex = Regex("(-[a-z0-9]{9})\$")
        val endsWithFreePrefix = freePrefixRegex.containsMatchIn(orgProfileName)
        return endsWithFreePrefix
    }

}
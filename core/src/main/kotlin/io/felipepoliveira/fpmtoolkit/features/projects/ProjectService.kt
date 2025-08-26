package io.felipepoliveira.fpmtoolkit.features.projects

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.ext.addError
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.projects.dto.CreateOrUpdateProjectDTO
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.SmartValidator
import java.time.LocalDateTime
import java.util.*

@Service
class ProjectService @Autowired constructor(
    private val projectDAO: ProjectDAO,
    private val organizationService: OrganizationService,
    private val organizationMemberService: OrganizationMemberService,
    smartValidator: SmartValidator,
    private val userService: UserService,
) : BaseService(smartValidator) {

    companion object {
        const val PAGINATION_LIMIT = 20
    }

    /**
     * Archive a project in the database
     */
    @Transactional
    fun archiveProject(requesterUuid: String, organizationUuid: String, projectUuid: String): ProjectModel {
        val requester = userService.assertFindByUuid(requesterUuid)
        val organization = organizationService.findByUuid(organizationUuid)

        // Assert that the requester has the required role
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        // update the target project in the database
        val projectToUpdate = projectDAO.findByOwnerAndUuid(organization, projectUuid) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "Could not find project '$projectUuid' on organization $organizationUuid"
        )

        // If the project is already archived throw an error
        if (projectToUpdate.archivedAt != null) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "Project is already archived"
            )
        }

        projectToUpdate.archivedAt = LocalDateTime.now()
        projectDAO.update(projectToUpdate)

        return projectToUpdate
    }

    /**
     * Create a new project into the platform
     */
    @Transactional
    fun createProject(requesterUuid: String, organizationUuid: String, dto: CreateOrUpdateProjectDTO): ProjectModel {
        // check for validation errors
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // Fetch request entities from database
        val requester = userService.assertFindByUuid(requesterUuid)
        val organization = organizationService.findByUuid(organizationUuid)

        // check if requester can make the operation
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        // check for duplicated project
        if (projectDAO.findByOwnerAndProfileName(organization, dto.profileName) != null) {
            validationResult.addError("profileName", "The 'profileName' '${dto.profileName}' can not be used")
            throw BusinessRuleException(validationResult)
        }

        // create the project so it can be registered in the database
        val project = ProjectModel(
            createdAt = LocalDateTime.now(),
            name = dto.name,
            id = null,
            uuid = UUID.randomUUID().toString(),
            profileName = dto.profileName,
            owner = organization,
            shortDescription = "",
            members = arrayListOf(),
            archivedAt = null,
        )
        projectDAO.persist(project)

        return project
    }

    /**
     * Return all ProjectModel associated with the given organization and the requester authorization over it.
     *
     * If the requester is a PROJECT_ADMINISTRATOR it can view all projects of the organization, otherwise, it can
     * only view the projects where it is a member
     */
    fun findByOrganization(
        requester: UserModel,
        organization: OrganizationModel,
        queryField: String? = null,
        page: Int = 1,
        limit: Int = PAGINATION_LIMIT,
    ): Collection<ProjectModel> {

        // if the requester has PROJECT_MANAGER privileges it can fetch all projects on the organization
        val requesterMembershipInOrg = organizationMemberService.findByOrganizationAndUserOrForbidden(
            organization,
            requester
        )

        // If the requester is a PROJECT_ADMINISTRATOR return all projects of the organization
        if (requesterMembershipInOrg.isOwnerOrAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)) {
            return projectDAO.findByOwnerIgnoreArchived(
                owner = organization,
                page = page.coerceAtLeast(1),
                itemsPerPage = limit.coerceIn(1..PAGINATION_LIMIT),
                queryField = queryField
            )
        }
        // otherwise, return only the projects where the requester is a member
        else {
            return projectDAO.findByOrganizationAndUserWithMembershipIgnoreArchived(
                owner = organization,
                user = requester,
                page = page.coerceAtLeast(1),
                itemsPerPage = limit.coerceIn(1..PAGINATION_LIMIT),
                queryField = queryField
            )
        }
    }

    /**
     * Find a project identified by the given UUID. If the project is not found throw BusinessRuleException(NOT_FOUND)
     */
    fun findByUuid(projectUuid: String) = projectDAO.findByUuid(projectUuid) ?: throw BusinessRuleException(
        BusinessRulesError.NOT_FOUND,
        "Could not find project identified by '$projectUuid'"
    )

    /**
     * Return pagination metadata of all ProjectModel associated with the given organization and the requester
     * authorization over it.
     *
     * If the requester is a PROJECT_ADMINISTRATOR it can view all projects of the organization, otherwise, it can
     * only view the projects where it is a member
     */
    fun paginationByOrganization(
        requester: UserModel,
        organization: OrganizationModel,
        queryField: String? = null,
        limit: Int = PAGINATION_LIMIT,
    ): Pagination {
        // if the requester has PROJECT_MANAGER privileges it can fetch all projects on the organization
        val requesterMembershipInOrg = organizationMemberService.findByOrganizationAndUserOrForbidden(
            organization,
            requester
        )

        // If the requester is a PROJECT_ADMINISTRATOR return all projects of the organization
        if (requesterMembershipInOrg.isOwnerOrAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)) {
            return projectDAO.paginationByOwnerIgnoreArchived(
                owner = organization,
                itemsPerPage = limit.coerceIn(1..PAGINATION_LIMIT),
                queryField = queryField
            )
        }
        // otherwise, return only the projects where the requester is a member
        else {
            return projectDAO.paginationByOrganizationAndUserWithMembershipIgnoreArchived(
                owner = organization,
                user = requester,
                itemsPerPage = limit.coerceIn(1..PAGINATION_LIMIT),
                queryField = queryField
            )
        }
    }

    fun findByProfileNameAndOrganization(
        requester: UserModel,
        organization: OrganizationModel,
        profileName: String
    ): ProjectModel {
        // check if the requester is a member of the organization
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)

        // return the project if found
        return projectDAO.findByOwnerAndProfileName(organization, profileName) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "Project $profileName was not found in organization ${organization.profileName}"
        )
    }

    /**
     * Archive a project in the database
     */
    @Transactional
    fun restoreArchivedProject(requesterUuid: String, organizationUuid: String, projectUuid: String): ProjectModel {
        val requester = userService.assertFindByUuid(requesterUuid)
        val organization = organizationService.findByUuid(organizationUuid)

        // Assert that the requester has the required role
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        // update the target project in the database
        val projectToUpdate = projectDAO.findByOwnerAndUuid(organization, projectUuid) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "Could not find project '$projectUuid' on organization $organizationUuid"
        )

        // If the project is already archived throw an error
        if (projectToUpdate.archivedAt == null) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "Project is not archived"
            )
        }

        projectToUpdate.archivedAt = null
        projectDAO.update(projectToUpdate)

        return projectToUpdate
    }

    @Transactional
    fun updateProject(
        requesterUuid: String, organizationUuid: String, projectUuid: String, dto: CreateOrUpdateProjectDTO
    ) : ProjectModel {

        // check for validation errors
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // Fetch request entities from database
        val requester = userService.assertFindByUuid(requesterUuid)
        val organization = organizationService.findByUuid(organizationUuid)

        // check if requester can make the operation
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        // check for duplicated project
        if (projectDAO.findByOwnerAndProfileName(organization, dto.profileName) != null) {
            validationResult.addError("profileName", "The 'profileName' '${dto.profileName}' can not be used")
            throw BusinessRuleException(validationResult)
        }

        // fetch the target updated project
        val updatedProject = projectDAO.findByOwnerAndUuid(organization, projectUuid) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            reason = "Could not find project identified by uuid '$projectUuid'"
        )


        // update the project in the database
        updatedProject.name = dto.name
        updatedProject.profileName = dto.profileName
        projectDAO.update(updatedProject)

        return updatedProject
    }
}
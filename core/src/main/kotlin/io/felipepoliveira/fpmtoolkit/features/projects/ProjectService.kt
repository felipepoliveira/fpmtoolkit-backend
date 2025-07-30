package io.felipepoliveira.fpmtoolkit.features.projects

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.ext.addError
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projects.dto.CreateOrUpdateProjectDTO
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.SmartValidator
import java.time.LocalDateTime
import java.util.*

@Service
class ProjectService @Autowired constructor(
    val projectDAO: ProjectDAO,
    val organizationMemberService: OrganizationMemberService,
    smartValidator: SmartValidator,
) : BaseService(smartValidator) {

    companion object {
        const val PAGINATION_LIMIT = 20
    }

    /**
     * Create a new project into the platform
     */
    fun createProject(requester: UserModel, organization: OrganizationModel, dto: CreateOrUpdateProjectDTO): ProjectModel {
        // check for validation errors
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

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
            members = arrayListOf()
        )
        projectDAO.persist(project)

        return project
    }

    fun findByOwner(
        requester: UserModel,
        organization: OrganizationModel,
        queryField: String? = null,
        page: Int = 1,
        limit: Int = PAGINATION_LIMIT,
    ): Collection<ProjectModel> {

        // if the requester has PROJECT_MANAGER privileges it can fetch all projects on the organization

        return projectDAO.findByOwner(
            owner = organization,
            page = page.coerceAtLeast(1),
            itemsPerPage = limit.coerceIn(1..PAGINATION_LIMIT),
            queryField = queryField
        )

        // otherwise it can only fetch projects where it is part on
    }
}
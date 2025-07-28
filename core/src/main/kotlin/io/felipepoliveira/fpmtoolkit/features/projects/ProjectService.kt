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
        )
        projectDAO.persist(project)

        return project
    }

}
package io.felipepoliveira.fpmtoolkit.features.projectMembers

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projectMembers.dto.AddProjectMemberByUserDTO
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.SmartValidator
import java.util.*

@Service
class ProjectMemberService @Autowired constructor(
    private val organizationMemberService: OrganizationMemberService,
    private val projectMemberDAO: ProjectMemberDAO,
    private val userService: UserService,
    smartValidator: SmartValidator,
) : BaseService(smartValidator)  {

    @Transactional
    fun addUserByOrganizationMembership(
        requester: UserModel, organization: OrganizationModel, project: ProjectModel, dto: AddProjectMemberByUserDTO
    ): ProjectMemberModel {

        // check for validation results
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // check for requester membership on organization
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        if (project.owner.id != organization.id) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "You do not have authorization to manage the project identified by '${project.uuid}'"
            )
        }

        // check if the target user UUID is a member of the organization
        val targetUser = userService.findByUuid(dto.userUuid)
        this.organizationMemberService.findByOrganizationAndUser(organization, targetUser)

        // check if the user is already a member
        if (projectMemberDAO.findByProjectAndUser(project, targetUser) != null) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "User identified by '${dto.userUuid}' is already a member of the project ${project.name}"
            )
        }

        val projectMember = ProjectMemberModel(
            user = targetUser,
            uuid = UUID.randomUUID().toString(),
            id =  null,
            project = project
        )
        projectMemberDAO.persist(projectMember)

        return projectMember
    }

    /**
     * Return members identified by the given UUIDs only if it is owned by the given project
     */
    fun findByUuid(
        project: ProjectModel,
        uuids: Collection<String>
    ): Collection<ProjectMemberModel> {

        val members = projectMemberDAO.findByProjectAndUuid(project, uuids)

        // Create a set of found UUIDs for fast lookup
        val foundUuids = members.map { it.uuid }.toSet()

        // Filter out the ones that were not found
        val notFoundUuids =  uuids.filterNot { it in foundUuids }

        if (notFoundUuids.isNotEmpty()) {
            throw BusinessRuleException(
                BusinessRulesError.NOT_FOUND,
                "Could not find members identified by ${notFoundUuids.joinToString(
                    ", ", "[", "]"
                )}"
            )
        }

        return members
    }

}
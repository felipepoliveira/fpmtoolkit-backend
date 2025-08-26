package io.felipepoliveira.fpmtoolkit.features.projectMembers

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projectMembers.dto.AddProjectMemberByUserDTO
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectService
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
    private val projectService: ProjectService,
    private val userService: UserService,
    smartValidator: SmartValidator,
) : BaseService(smartValidator)  {

    companion object {
        const val PAGINATION_LIMIT = 20
    }

    @Transactional
    fun addUserByOrganizationMembership(
        requesterUuid: String, projectUuid: String, dto: AddProjectMemberByUserDTO
    ) = addUserByOrganizationMembership(
        userService.assertFindByUuid(requesterUuid),
        projectService.findByUuid(projectUuid),
        dto
    )

    @Transactional
    internal fun addUserByOrganizationMembership(
        requester: UserModel, project: ProjectModel, dto: AddProjectMemberByUserDTO
    ): ProjectMemberModel {

        // check for validation results
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // check for requester membership on organization
        organizationMemberService.findByOrganizationAndUserOrForbidden(project.owner, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        // check if the target user UUID is a member of the organization
        val targetUser = userService.findByUuid(dto.userUuid)
        this.organizationMemberService.findByOrganizationAndUser(project.owner, targetUser)

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

    fun findByProject(
        requesterUuid: String, projectUuid: String, page: Int, limit: Int, queryField: String?
    ): Collection<ProjectMemberModel> {
        // check if the requester is a member of the organization of the project
        val requester = userService.assertFindByUuid(requesterUuid)
        val project = projectService.findByUuid(projectUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(project.owner, requester)

        //
        return projectMemberDAO.findByProject(
            project,
            page.coerceAtLeast(1),
            limit.coerceIn(1..PAGINATION_LIMIT),
            queryField
        )
    }

    fun paginationByProject(
        requesterUuid: String, projectUuid: String, limit: Int, queryField: String?
    ): Pagination {
        // check if the requester is a member of the organization of the project
        val requester = userService.assertFindByUuid(requesterUuid)
        val project = projectService.findByUuid(projectUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(project.owner, requester)

        //
        return projectMemberDAO.paginationByProject(
            project,
            limit.coerceIn(1..PAGINATION_LIMIT),
            queryField
        )
    }

    /**
     * Return members identified by the given UUIDs only if it is owned by the given project
     */
    fun findByProjectAndUuid(
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

    /**
     * Remove the target member identified by its UUID. The given requester should have the authorizations:
     * - Must be the organization owner, or
     * - Must be an organization administrator, or
     * - Must have a PROJECT_ADMINISTRATOR role
     */
    @Transactional
    fun removeMember(
        requesterUuid: String,
        targetMemberUuid: String
    ): ProjectMemberModel {

        // fetch the target member to delete, throw error if not found
        val targetMember = projectMemberDAO.findByUuid(targetMemberUuid) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "Could not find a member identified by '$targetMemberUuid'"
        )

        // check if the requester user has authorization to make the process
        val requester = userService.assertFindByUuid(requesterUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(targetMember.project.owner, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        // remove
        projectMemberDAO.delete(targetMember)

        return targetMember
    }

}
package io.felipepoliveira.fpmtoolkit.features.projectDeliverables

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.ext.addError
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.dao.CreateOrUpdateProjectDeliverableDTO
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberService
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectService
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.BindingResult
import org.springframework.validation.SmartValidator
import java.util.*

@Service
class ProjectDeliverableService @Autowired constructor (
    private val organizationMemberService: OrganizationMemberService,
    private val projectDeliverableDAO: ProjectDeliverableDAO,
    private val projectService: ProjectService,
    private val projectMemberService: ProjectMemberService,
    private val userService: UserService,
    smartValidator: SmartValidator
) : BaseService(smartValidator) {

    companion object {
        const val PAGINATION_LIMIT = 30
    }

    private fun businessRulesValidationsOnCreateDTO(
        dto: CreateOrUpdateProjectDeliverableDTO,
        validationResult: BindingResult): BindingResult {

        // check for expected range validation errors
        if (dto.expectedStartDate != null && dto.expectedEndDate != null &&
            dto.expectedStartDate > dto.expectedEndDate) {
            validationResult.addError(
                "expectedStartDate",
                "Field 'expectedStartDate' should be lesser than 'expectedEndDate'"
            )
        }

        // check for factual range validation errors
        if (dto.factualStartDate != null && dto.factualEndDate != null &&
            dto.factualStartDate >= dto.factualEndDate) {
            validationResult.addError(
                "factualStartDate",
                "Field 'factualStartDate' should be lesser than 'factualEndDate'"
            )
        }

        return validationResult
    }

    private fun validatePredecessorRedundancy(
        dto: CreateOrUpdateProjectDeliverableDTO,
        validationResult: BindingResult,
        updatedDeliverable: ProjectDeliverableModel
    ): BindingResult {
        // if any of the predecessors on DTO already have a reference of the updatedDeliverable as a predecessor throw
        // an error
        val deepPredecessors = updatedDeliverable.predecessors.filter { p -> p.isDeepPredecessor(updatedDeliverable) }
        if (deepPredecessors.isNotEmpty()) {
            for (badPredecessor in deepPredecessors) {

                // identify the index of the bad predecessors on DTO
                val indexOfBadPredecessor = dto.predecessors.indexOfFirst { it == badPredecessor.uuid }
                if (indexOfBadPredecessor < 0) {
                    continue
                }

                // add the validation error
                validationResult.addError(
                    "predecessors[$indexOfBadPredecessor]",
                    "Predecessor '${badPredecessor.name}' identified by '${badPredecessor.uuid}' " +
                            "already has a predecessor of the updated deliverable '${updatedDeliverable.name}'"
                )
            }
        }


        return validationResult
    }

    @Transactional
    fun createDeliverable(
        requesterUuid: String,
        projectUuid: String,
        dto: CreateOrUpdateProjectDeliverableDTO,
    ): ProjectDeliverableModel {
        // validate DTO
        val validationResult =  businessRulesValidationsOnCreateDTO(dto, validate(dto))
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        val requester = userService.assertFindByUuid(requesterUuid)
        val project = projectService.findByUuid(projectUuid)

        // check membership authorization
        organizationMemberService.findByOrganizationAndUserOrForbidden(project.owner, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        // check if there is no duplicated name
        if (projectDeliverableDAO.findByNameAndProject(dto.name, project) != null) {
            validationResult.addError("name", "Name '${dto.name}' is already used by another deliverable")
        }

        // check for business rule validation errors
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // Create the new deliverable
        val deliverable = ProjectDeliverableModel(
            id = null,
            uuid = UUID.randomUUID().toString(),
            project = project,
            name = dto.name,
            factualStartDate = dto.factualStartDate,
            expectedStartDate = dto.expectedStartDate,
            factualEndDate = dto.factualEndDate,
            predecessors = findByUuid(project, dto.predecessors),
            expectedEndDate = dto.expectedEndDate,
            responsible = projectMemberService.findByProjectAndUuid(project, dto.responsible)
        )

        projectDeliverableDAO.persist(deliverable)

        return deliverable
    }

    /**
     * Find all ProjectDeliverableModel owned by the project identified by projectUuid. This method
     * uses pagination
     */
    fun findByProject(
        requesterUuid: String, projectUuid: String, limit: Int, page: Int, query: String?
    ): Collection<ProjectDeliverableModel> {

        // assert that the given requester is a member of the organization that owns the project
        val requester = userService.assertFindByUuid(requesterUuid)
        val project = projectService.findByUuid(projectUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(project.owner, requester)

        return projectDeliverableDAO.findByProject(
            project,
            limit.coerceIn(1..PAGINATION_LIMIT),
            page.coerceAtLeast(1),
            query
            )
    }

    /**
     * Return pagination metadata of all ProjectDeliverableModel owned by the project identified by projectUuid
     */
    fun paginationByProject(
        requesterUuid: String, projectUuid: String, limit: Int, query: String?
    ): Pagination {

        // assert that the given requester is a member of the organization that owns the project
        val requester = userService.assertFindByUuid(requesterUuid)
        val project = projectService.findByUuid(projectUuid)
        organizationMemberService.findByOrganizationAndUserOrForbidden(project.owner, requester)

        return projectDeliverableDAO.paginationByProject(
            project,
            limit.coerceIn(1..PAGINATION_LIMIT),
            query
        )
    }

    fun findByUuid(uuid: String): ProjectDeliverableModel {
        return projectDeliverableDAO.findByUuid(uuid) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "Could not find deliverable identified by '$uuid'"
        )
    }

    /**
     * Find all deliverables identified by the given UUID and project. If any of the given UUID is not found
     * an BusinessRuleException(NOT_FOUND) will be thrown
     */
    fun findByUuid(
        project: ProjectModel,
        uuids: Collection<String>
    ): Collection<ProjectDeliverableModel> {

        val deliverables = projectDeliverableDAO.findByProjectAndUuid(project, uuids)

        // Create a set of found UUIDs for fast lookup
        val foundUuids = deliverables.map { it.uuid }.toSet()

        // Filter out the ones that were not found
        val notFoundUuids =  uuids.filterNot { it in foundUuids }

        if (notFoundUuids.isNotEmpty()) {
            throw BusinessRuleException(
                BusinessRulesError.NOT_FOUND,
                "Could not find deliverables identified by ${notFoundUuids.joinToString(
                    ", ", "[", "]"
                )}"
            )
        }

        return deliverables
    }

    /**
     * TODO Unit tests pending on updateDeliverable
     *
     */
    @Transactional
    fun updateDeliverable(
        requesterUuid: String,
        deliverableUuid: String,
        dto: CreateOrUpdateProjectDeliverableDTO,
    ): ProjectDeliverableModel {
        // validate DTO
        val validationResult =  businessRulesValidationsOnCreateDTO(dto, validate(dto))
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        val requester = userService.assertFindByUuid(requesterUuid)
        val deliverable = findByUuid(deliverableUuid)

        // check membership authorization
        organizationMemberService.findByOrganizationAndUserOrForbidden(deliverable.project.owner, requester)
            .assertIsOwnerOrOrganizationAdministratorOr(OrganizationMemberRoles.ORG_PROJECT_ADMINISTRATOR)

        // check if there is no duplicated name
        val duplicatedDeliverable = projectDeliverableDAO.findByNameAndProject(dto.name, deliverable.project)
        if (duplicatedDeliverable != null && duplicatedDeliverable.id != deliverable.id) {
            validationResult.addError("name", "Name '${dto.name}' is already used by another deliverable")
        }

        // check for business rule validation errors
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // Update the deliverable
        deliverable.apply {
            name = dto.name
            factualStartDate = dto.factualStartDate
            expectedStartDate = dto.expectedStartDate
            factualEndDate = dto.factualEndDate
            expectedEndDate = dto.expectedEndDate
            predecessors = findByUuid(project, dto.predecessors)
            responsible = projectMemberService.findByProjectAndUuid(project, dto.responsible)
        }

        // check for redundancy errors
        validatePredecessorRedundancy(dto, validationResult, deliverable)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        projectDeliverableDAO.update(deliverable)

        return deliverable
    }
}
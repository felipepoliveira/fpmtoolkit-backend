package io.felipepoliveira.fpmtoolkit.features.projectDeliverables

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.ext.addError
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.dao.CreateOrUpdateProjectDeliverableDTO
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberService
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.BindingResult
import org.springframework.validation.SmartValidator
import java.util.*

class ProjectDeliverableService @Autowired constructor (
    private val organizationMemberService: OrganizationMemberService,
    private val projectDeliverableDAO: ProjectDeliverableDAO,
    private val projectMemberService: ProjectMemberService,
    smartValidator: SmartValidator
) : BaseService(smartValidator) {

    private fun businessRulesValidationsOnDTO(
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

    @Transactional
    fun createDeliverable(
        requester: UserModel,
        organization: OrganizationModel,
        project: ProjectModel,
        dto: CreateOrUpdateProjectDeliverableDTO,
    ): ProjectDeliverableModel {
        // validate DTO
        val validationResult =  businessRulesValidationsOnDTO(dto, validate(dto))
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // check membership authorization
        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
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
            responsible = projectMemberService.findByUuid(project, dto.responsible)
        )

        projectDeliverableDAO.persist(deliverable)

        return deliverable
    }
}
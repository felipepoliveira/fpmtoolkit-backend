package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableService
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.dao.CreateOrUpdateProjectDeliverableDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/projects/{projectUuid}/deliverables")
class ProjectDeliverableController @Autowired constructor(
    private val projectDeliverableService: ProjectDeliverableService
)  : BaseRestController(){

    /**
     * Return deliverables of the given project or return its pagination metadata representation
     */
    @GetMapping
    fun fetchOrPaginationByProject(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable projectUuid: String,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) pagination: Boolean?
    ) = ok {
        if (pagination != null && pagination == true) {
            projectDeliverableService.paginationByProject(
                requestClient.userIdentifier,
                projectUuid,
                limit ?: ProjectDeliverableService.PAGINATION_LIMIT,
                query
            )
        }
        else {
            projectDeliverableService.findByProject(
                requestClient.userIdentifier,
                projectUuid,
                limit ?: ProjectDeliverableService.PAGINATION_LIMIT,
                page ?: 1,
                query
            )
        }
    }

    /**
     * Crete a new deliverable in the project
     */
    @PostMapping
    fun createDeliverable(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable projectUuid: String,
        @RequestBody dto: CreateOrUpdateProjectDeliverableDTO
    ) = ok {
        projectDeliverableService.createDeliverable(
            requestClient.userIdentifier,
            projectUuid,
            dto
        )
    }

    @PutMapping("/{deliverableUuid}")
    fun updateDeliverable(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable deliverableUuid: String,
        @RequestBody dto: CreateOrUpdateProjectDeliverableDTO,
    ) = ok {
        projectDeliverableService.updateDeliverable(
            requestClient.userIdentifier,
            deliverableUuid,
            dto
        )
    }

}
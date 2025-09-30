package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberService
import io.felipepoliveira.fpmtoolkit.features.projectMembers.dto.AddProjectMemberByUserDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projects/{projectUuid}/members")
class ProjectMemberController @Autowired constructor(
    private val projectMemberService: ProjectMemberService,
): BaseRestController() {

    /**
     * Add a new member into the project
     */
    @PostMapping
    fun addUserByOrganizationMembership(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable projectUuid: String,
        @RequestBody dto: AddProjectMemberByUserDTO,
    ): ResponseEntity<Any> {
        return ok {
            projectMemberService.addUserByOrganizationMembership(
                requestClient.userIdentifier,
                projectUuid,
                dto,
            )
        }
    }

    /**
     * Return data or pagination metadata of the members of the project
     */
    @GetMapping
    fun findDataOrPagination(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable projectUuid: String,
        @RequestParam(name = "pagination", required = false) pagination: Boolean?,
        @RequestParam(name = "limit", required = false) limit: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "query", required = false) query: String?,
    ) = ok {
        if (pagination != null && pagination) {
            projectMemberService.paginationByProject(
                requestClient.userIdentifier,
                projectUuid,
                limit ?: ProjectMemberService.PAGINATION_LIMIT,
                query
            )
        } else {
            projectMemberService.findByProject(
                requestClient.userIdentifier,
                projectUuid,
                page ?: 1,
                limit ?: ProjectMemberService.PAGINATION_LIMIT,
                query
            )
        }
    }

    /**
     * Remove a member from the project
     */
    @DeleteMapping("/{memberUuid}")
    fun removeMember(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable projectUuid: String,
        @PathVariable memberUuid: String,
    ) = ok {
        projectMemberService.removeMember(
            requestClient.userIdentifier,
            memberUuid,
        )
    }

}
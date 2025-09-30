package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectService
import io.felipepoliveira.fpmtoolkit.features.projects.dto.CreateOrUpdateProjectDTO
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organizations/{organizationUuid}/projects")
class ProjectController @Autowired constructor(
    private val organizationService: OrganizationService,
    private val projectService: ProjectService,
    private val userService: UserService,
) : BaseRestController(){

    @DeleteMapping("/{projectUuid}")
    fun archiveProject(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUuid: String,
        @PathVariable(name = "projectUuid") projectUuid: String
    ) = ok {
        projectService.archiveProject(
            requesterUuid = requestClient.userIdentifier,
            organizationUuid = organizationUuid,
            projectUuid = projectUuid,
        )
    }


    /**
     * Create a new project into the platform
     */
    @PostMapping
    fun createProject(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable organizationUuid: String,
        @RequestBody dto: CreateOrUpdateProjectDTO,
    ) = ok {
        projectService.createProject(requestClient.userIdentifier, organizationUuid, dto)
    }

    @GetMapping("/find-by-profile-name/{profileName}")
    fun findByProfileName(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUuid: String,
        @PathVariable profileName: String
    ) = ok {
        projectService.findByProfileNameAndOrganization(
            userService.assertFindByUuid(requestClient.userIdentifier),
            organizationService.findByUuid(organizationUuid),
            profileName
        )
    }

    /**
     * Find all projects of an organization
     */
    @GetMapping
    fun findDataOrPaginationByOrganization(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable organizationUuid: String,
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pagination: Boolean?,
        @RequestParam(required = false) queryField: String?,
    ) = ok {
        if (pagination == true) {
            projectService.paginationByOrganization(
                requester = userService.assertFindByUuid(requestClient.userIdentifier),
                organization = organizationService.findByUuid(organizationUuid),
                queryField = queryField,
                limit = limit ?: ProjectService.PAGINATION_LIMIT
            )
        }
        else {
            projectService.findByOrganization(
                requester = userService.assertFindByUuid(requestClient.userIdentifier),
                organization = organizationService.findByUuid(organizationUuid),
                queryField = queryField,
                page = page ?: 1,
                limit = limit ?: ProjectService.PAGINATION_LIMIT
            )
        }
    }

    /**
     * Restore project from archive
     */
    @PutMapping("/{projectUuid}/restore")
    fun restoreArchivedProject(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUuid: String,
        @PathVariable(name = "projectUuid") projectUuid: String
    ) = ok {
        projectService.restoreArchivedProject(
            requesterUuid = requestClient.userIdentifier,
            organizationUuid = organizationUuid,
            projectUuid = projectUuid,
        )
    }

    /**
     * Update a project in the database
     */
    @PutMapping("/{projectUuid}")
    fun updateProject(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUuid: String,
        @PathVariable(name = "projectUuid") projectUuid: String,
        @RequestBody dto: CreateOrUpdateProjectDTO
    ) = ok {
        projectService.updateProject(
            requesterUuid = requestClient.userIdentifier,
            projectUuid = projectUuid,
            organizationUuid = organizationUuid,
            dto = dto
        )
    }
}
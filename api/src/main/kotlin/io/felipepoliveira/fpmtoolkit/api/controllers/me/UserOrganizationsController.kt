package io.felipepoliveira.fpmtoolkit.api.controllers.me

import io.felipepoliveira.fpmtoolkit.api.controllers.BaseRestController
import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.organizations.dto.CreateOrganizationDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/me/organizations")
class UserOrganizationsController @Autowired constructor(
    private val organizationService: OrganizationService,
) : BaseRestController() {

    /**
     * Create a new organization into the platform
     */
    @PostMapping
    fun createOrganization(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: CreateOrganizationDTO,
    ) = ok {
        organizationService.createOrganization(requestClient.userIdentifier, dto)
    }

    /**
     * Return all organizations where the user is a member
     */
    @GetMapping
    fun fetchOrganizationFromRequesterClient(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestParam(name = "limit", required = false) limit: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
    ) = ok {
        organizationService.findOrganizationsByMember(
            requestClient.userIdentifier,
            limit ?: OrganizationService.PAGINATION_LIMIT,
            page ?: 1
        )
    }

}
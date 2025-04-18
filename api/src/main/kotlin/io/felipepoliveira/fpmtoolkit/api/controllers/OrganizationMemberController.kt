package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/organizations/{organizationUuid}/members")
class OrganizationMemberController @Autowired constructor(
    private val organizationService: OrganizationService,
    private val organizationMemberService: OrganizationMemberService,
    private val userService: UserService,
) : BaseRestController() {

    @GetMapping
    fun findOrPaginationByOrganization(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUuid: String,
        @RequestParam(name = "pagination", required = false) returnPagination: Boolean?,
        @RequestParam(name = "itemsPerPage", required = false) itemsPerPage: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
    ) = ok {
        if (returnPagination == true) {
            organizationMemberService.paginationByOrganization(
                organizationService.findByUuidAndCheckIfUserIsAMember(requestClient.userIdentifier, organizationUuid),
                userService.findByUuid(requestClient.userIdentifier),
                itemsPerPage ?: OrganizationMemberService.PAGINATION_LIMIT
            )
        }
        else {
            organizationMemberService.findByOrganization(
                organizationService.findByUuidAndCheckIfUserIsAMember(requestClient.userIdentifier, organizationUuid),
                userService.findByUuid(requestClient.userIdentifier),
                itemsPerPage ?: OrganizationMemberService.PAGINATION_LIMIT,
                page ?: 1
            )
        }

    }
}
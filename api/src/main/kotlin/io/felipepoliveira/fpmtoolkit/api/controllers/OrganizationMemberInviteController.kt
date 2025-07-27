package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.controllers.dto.SendOrganizationMemberInviteMailDTO
import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.api.security.auth.Roles
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteService
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.dto.CreateOrganizationMemberInviteDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/organization-member-invites/public")
class OrganizationMemberInvitePublicEndpointsController  @Autowired constructor(
    val organizationMemberInviteService: OrganizationMemberInviteService,
): BaseRestController(){

    @GetMapping("/by-token/{token}")
    fun fetchInviteOrganizationUsingToken(
        @PathVariable token: String
    ) = ok {
        organizationMemberInviteService.findByInviteToken(token)
    }
}

@RestController
@RequestMapping("/api/organizations/{organizationUuid}/invites")
class OrganizationMemberInviteController @Autowired constructor(
    private val organizationMemberInviteService: OrganizationMemberInviteService
) : BaseRestController() {


    @PostMapping
    fun createInvite(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUUID: String,
        @RequestBody dto: CreateOrganizationMemberInviteDTO
    ) = ok {
        organizationMemberInviteService.create(requestClient.userIdentifier, organizationUUID, dto)
    }

    @GetMapping
    fun fetchOrPaginationOfInvitesFromOrganization(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUUID: String,
        @RequestParam(name = "limit", required = false) limit: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "pagination", required = false) returnPagination: Boolean?,
        @RequestParam(name = "queryField", required = false) queryField: String?,
    ) = ok {
        if (returnPagination == true) {
            organizationMemberInviteService.paginationByOrganization(
                requestClient.userIdentifier,
                organizationUUID,
                limit ?: OrganizationMemberInviteService.PAGINATION_LIMIT,
                queryField
            )
        } else {
            organizationMemberInviteService.findByOrganization(
                requestClient.userIdentifier,
                organizationUUID,
                limit ?: OrganizationMemberInviteService.PAGINATION_LIMIT,
                page ?: 1,
                queryField
            )
        }
    }

    @DeleteMapping("/{inviteUuid}")
    fun remove(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable("inviteUuid") inviteUUID: String,
    ) = ok {
        organizationMemberInviteService.remove(requestClient.userIdentifier, inviteUUID)
    }

    @PostMapping("/{inviteUuid}/resend-mail")
    fun sendInviteMail(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: SendOrganizationMemberInviteMailDTO,
        @PathVariable inviteUuid: String
    ) = noContent {
        organizationMemberInviteService.sendInviteMail(requestClient.userIdentifier, inviteUuid, dto.mailLanguage)
    }
}
package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.api.security.auth.Roles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.UpdateOrganizationMemberDTO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/organization-members/public")
class OrganizationMemberPublicEndpointsController @Autowired constructor(
    private val organizationMemberService: OrganizationMemberService,
) : BaseRestController() {
    /**
     * Add a new organization member into an organization using a invite as a ingress
     */
    @PostMapping("/ingress-by-invite")
    fun ingressByInvite(
        @RequestBody dto: IngressByInviteDTO
    ) = ok {
        organizationMemberService.ingressByInvite(dto.token)
    }
}

@RestController
@RequestMapping("/api/organizations/{organizationUuid}/members")
class OrganizationMemberController @Autowired constructor(
    private val organizationService: OrganizationService,
    private val organizationMemberService: OrganizationMemberService,
    private val userService: UserService,
) : BaseRestController() {

    /**
     * Transform the target member (identified by 'targetMemberUuid') into the organization owner. This operation
     * can only be made by the organization owner with the top tier STL level
     */
    @PutMapping("/{targetMemberUuid}/set-to-owner")
    @Secured(Roles.STL_MOST_SECURE)
    fun changeOrganizationOwner(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "targetMemberUuid") targetMemberUuid: String
    ) = ok {
        //
        organizationMemberService.changeOrganizationOwner(
            requestClient.userIdentifier,
            targetMemberUuid
        )
    }

    /**
     *
     */
    @GetMapping
    fun findOrPaginationByOrganization(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUuid: String,
        @RequestParam(name = "pagination", required = false) returnPagination: Boolean?,
        @RequestParam(name = "itemsPerPage", required = false) itemsPerPage: Int?,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "queryField", required = false) queryField: String?,
    ) = ok {
        if (returnPagination == true) {
            organizationMemberService.paginationByOrganization(
                organizationService.findByUuidAndCheckIfUserIsAMember(requestClient.userIdentifier, organizationUuid),
                userService.findByUuid(requestClient.userIdentifier),
                itemsPerPage ?: OrganizationMemberService.PAGINATION_LIMIT,
                queryField,
            )
        }
        else {
            organizationMemberService.findByOrganization(
                organizationService.findByUuidAndCheckIfUserIsAMember(requestClient.userIdentifier, organizationUuid),
                userService.findByUuid(requestClient.userIdentifier),
                itemsPerPage ?: OrganizationMemberService.PAGINATION_LIMIT,
                page ?: 1,
                queryField
            )
        }
    }

    /**
     * Return the membership account of the requester client in the given organization
     */
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable(name = "organizationUuid") organizationUuid: String,
    ) = ok {
        val organization = organizationService.findByUuid(organizationUuid)
        val requester = userService.assertFindByUuid(requestClient.userIdentifier)

        organizationMemberService.findByOrganizationAndUserOrForbidden(organization, requester)
    }

    /**
     * Delete the user identified by the 'targetMemberUuid' parameter
     */
    @DeleteMapping("/{targetMemberUuid}")
    fun removeOrganizationMember(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable organizationUuid: String,
        @PathVariable targetMemberUuid: String
    ) = ok {
        organizationMemberService.removeOrganizationMember(
            requestClient.userIdentifier,
            organizationUuid,
            targetMemberUuid
        )
    }

    /**
     * Update the user identified by the 'targetMemberUuid' parameter
     */
    @PutMapping("/{targetMemberUuid}")
    fun update(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable targetMemberUuid: String,
        @RequestBody dto: UpdateOrganizationMemberDTO
    ) = ok {
        organizationMemberService.update(requestClient.userIdentifier, targetMemberUuid, dto)
    }
}

/**
 * DTO used in /public/ingress-by-invite endpoint
 */
data class IngressByInviteDTO(
    /**
     * The token
     */
    @field:NotBlank
    val token: String,
)
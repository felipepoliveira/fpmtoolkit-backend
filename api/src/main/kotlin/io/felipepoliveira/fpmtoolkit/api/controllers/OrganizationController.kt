package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.organizations.dto.CreateOrganizationDTO
import io.felipepoliveira.fpmtoolkit.features.organizations.dto.UpdateOrganizationDTO
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * Used in is profile name available service request
 */
data class IsProfileNameAvailableRequest(
    @field:NotBlank
    val profileName: String,
)

/**
 * Used in is profile name available service request
 */
data class IsProfileNameAvailableResponse(
    val profileName: String,
    val isAvailable: Boolean
)

@RestController
@RequestMapping("/api/organizations")
class OrganizationController @Autowired constructor(
    private val organizationService: OrganizationService
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

    @GetMapping("/{uuid}")
    fun findByUuid(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable uuid: String
    ) = ok {
        organizationService.findByUuidAndCheckIfUserIsAMember(requestClient.userIdentifier, uuid)
    }

    @GetMapping("/find-by-profile-name/{profileName}")
    fun findByProfileName(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable profileName: String
    ) = ok {
        organizationService.findByProfileNameAndCheckIfUserIsAMember(requestClient.userIdentifier, profileName)
    }

    /**
     * Check if the profile name is available
     */
    @GetMapping("/is-profile-name-available")
    fun isProfileNameAvailable(
        @RequestBody request: IsProfileNameAvailableRequest
    ) = ok {
        IsProfileNameAvailableResponse(
            profileName = request.profileName,
            isAvailable = organizationService.isProfileNameAvailable(request.profileName)
        )
    }

    /**
     * Update a organization identified by its UUID
     */
    @PutMapping("/{uuid}")
    fun updateOrganization(
        @AuthenticationPrincipal requestClient: RequestClient,
        @PathVariable uuid: String,
        @RequestBody dto: UpdateOrganizationDTO
    ) = ok {
        organizationService.updateOrganization(requestClient.userIdentifier, uuid, dto)
    }
}
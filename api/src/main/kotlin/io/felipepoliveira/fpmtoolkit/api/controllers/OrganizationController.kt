package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class IsProfileNameAvailableRequest(
    val profileName: String,
)

@RestController
@RequestMapping("/api/organizations")
class OrganizationController @Autowired constructor(
    private val organizationService: OrganizationService
) : BaseRestController() {

    @GetMapping("/is-profile-name-available")
    fun isProfileNameAvailable() = ok {

    }
}
package io.felipepoliveira.fpmtoolkit.api.controllers.me

import io.felipepoliveira.fpmtoolkit.api.controllers.BaseRestController
import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.features.users.dto.UpdatePasswordDTO
import io.felipepoliveira.fpmtoolkit.features.users.dto.UpdateUserDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller used to manage data of the authenticated user
 */
@RestController
@RequestMapping("/api/me")
class MeController @Autowired constructor(
    private val userService: UserService,
) : BaseRestController() {

    @PutMapping("/password")
    fun updatePassword(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: UpdatePasswordDTO,
    ) = ok {
        userService.updatePassword(requestClient.userIdentifier, dto)
    }

    /**
     * Update the authenticated user account data
     */
    @PutMapping
    fun updateUser(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: UpdateUserDTO,
    ) = ok {
        userService.updateUser(requestClient.userIdentifier, dto)
    }

}
package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.features.users.dto.CreateUserDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController @Autowired constructor(
    private val userService: UserService,
) : BaseRestController() {

    /**
     * Create a user account in the platform
     */
    @PostMapping("/public")
    fun createUserAccount(
        @RequestBody dto: CreateUserDTO
    ) = ok { userService.createUser(dto) }

}
package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.api.security.tokens.ApiAuthenticationTokenProvider
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.features.users.dto.FindByPrimaryEmailAndPasswordDTO
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneOffset

@RestController
@RequestMapping("/api/auth")
class AuthenticationController @Autowired constructor(
    private val apiAuthenticationTokenProvider: ApiAuthenticationTokenProvider,
    private val userService: UserService,
) : BaseRestController() {

    /**
     * Create an authentication token using email and password combination
     */
    @PostMapping("/public/tokens/email-and-password")
    fun generateAuthenticationToken(
        @RequestBody dto: FindByPrimaryEmailAndPasswordDTO,
        request: HttpServletRequest
    ) = ok {
        val user = userService.findByPrimaryEmailAndPassword(dto)
        val tokenAndPayload = apiAuthenticationTokenProvider.issue(
            user = user,
            clientIdentifier = request.remoteAddr,
            expiresAt = LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.UTC),
            roles = getSessionRoles(user)
        )
        tokenAndPayload
    }

    /**
     * Return the session data of the authenticated user
     */
    @GetMapping("/session")
    fun getRequestClientSession(@AuthenticationPrincipal client: RequestClient) = ok { client }

    /**
     * Return the roles of the given user.
     */
    private fun getSessionRoles(user: UserModel): Array<String> = arrayOf() //TODO implement roles aggregation on token

}
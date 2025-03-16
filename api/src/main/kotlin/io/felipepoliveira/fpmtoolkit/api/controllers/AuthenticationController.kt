package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.api.controllers.dto.SendPasswordRecoveryMailDTO
import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.api.security.auth.Roles
import io.felipepoliveira.fpmtoolkit.api.security.tokens.ApiAuthenticationTokenProvider
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.features.users.dto.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
     * Confirm the user primary email using the token
     */
    @PutMapping("/confirm-primary-email-with-token")
    fun confirmPrimaryEmailWithToken(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: ConfirmPrimaryEmailWithTokenDTO
    ) = ok { userService.confirmPrimaryEmailWithToken(dto) }

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
    

    /**
     * Return information about the authenticated user
     */
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal requestClient: RequestClient,
    ) = ok { userService.assertFindByUuid(requestClient.userIdentifier) }

    /**
     * Send the primary email change mail
     */
    @PostMapping("/send-primary-email-change-mail")
    @Secured(Roles.STL_SECURE)
    fun sendPrimaryEmailChangeMail(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: SendPrimaryEmailChangeMailDTO
    ) = ok {
        userService.sendPrimaryEmailChangeMail(requestClient.userIdentifier, dto)
    }

    /**
     * Send the primary email confirmation mail
     */
    @PostMapping("/send-primary-email-confirmation-mail")
    fun sendPrimaryEmailChangeMail(
        @AuthenticationPrincipal requestClient: RequestClient,
    ) = noContent {
        userService.sendPrimaryEmailConfirmationMail(requestClient.userIdentifier)
    }

    /**
     * Send the password recovery mail
     */
    @PostMapping("/public/send-password-recovery-mail")
    fun sendPasswordRecoveryMail(
        @RequestBody dto: SendPasswordRecoveryMailDTO
    ) = noContent {
        userService.sendPasswordRecoveryMail(dto.primaryEmail)
    }

    /**
     * Update the user password using the recovery mail
     */
    @PutMapping("/public/update-password-with-recovery-token")
    fun updatePasswordWithRecoveryToken(
        @RequestBody dto: UpdatePasswordWithRecoveryTokenDTO
    ) = ok {
        userService.updatePasswordWithRecoveryToken(dto)
    }

    /**
     * Update the primary email with token
     */
    @PutMapping("/update-primary-email-with-token")
    fun updatePrimaryEmailWithPrimaryEmailChangeToken(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: UpdatePrimaryEmailWithPrimaryEmailChangeTokenDTO
    ) = ok {
        userService.updatePrimaryEmailWithPrimaryEmailChangeToken(requestClient.userIdentifier, dto)
    }

}
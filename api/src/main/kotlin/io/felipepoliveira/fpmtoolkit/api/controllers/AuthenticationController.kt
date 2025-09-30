package io.felipepoliveira.fpmtoolkit.api.controllers

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.api.controllers.dto.AuthenticateWithPasswordDTO
import io.felipepoliveira.fpmtoolkit.api.controllers.dto.RefreshTokenDTO
import io.felipepoliveira.fpmtoolkit.api.controllers.dto.SendPasswordRecoveryMailDTO
import io.felipepoliveira.fpmtoolkit.api.security.auth.RequestClient
import io.felipepoliveira.fpmtoolkit.api.security.auth.Roles
import io.felipepoliveira.fpmtoolkit.api.security.tokens.ApiAuthenticationTokenProvider
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.features.users.dto.*
import io.felipepoliveira.fpmtoolkit.security.comparePassword
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/auth")
class AuthenticationController @Autowired constructor(
    private val apiAuthenticationTokenProvider: ApiAuthenticationTokenProvider,
    private val organizationService: OrganizationService,
    private val organizationMemberService: OrganizationMemberService,
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
    fun generateAuthenticationTokenWithEmailAndPassword(
        @RequestBody dto: FindByPrimaryEmailAndPasswordDTO,
        request: HttpServletRequest
    ) = ok {
        val user = userService.findByPrimaryEmailAndPassword(dto)
        val tokenAndPayload = apiAuthenticationTokenProvider.issue(
            user = user,
            clientIdentifier = request.remoteAddr,
            expiresAt = LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.UTC),
            roles = getSessionRoles(user),
            null
        )
        tokenAndPayload
    }

    @PostMapping("/tokens/password")
    fun generateAuthenticationTokenWithPassword(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: AuthenticateWithPasswordDTO,
        request: HttpServletRequest
    ) = ok {

        // authenticate the user using the given password
        val user = userService.assertFindByUuid(requestClient.userIdentifier)
        if (!comparePassword(dto.password, user.hashedPassword)) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "Invalid credentials"
            )
        }

        // issue a new authentication token
        val tokenAndPayload = apiAuthenticationTokenProvider.issue(
            user = user,
            clientIdentifier = request.remoteAddr,
            expiresAt = LocalDateTime.now().plusDays(30).toInstant(ZoneOffset.UTC),
            roles = getSessionRoles(user),
            null
        )
        tokenAndPayload

    }

    private fun getOrganizationMemberRoles(organizationId: String, requesterUser: UserModel): Array<String> {
        val organization = organizationService.findByUuid(organizationId)

        val organizationMembership = organizationMemberService.findByOrganizationAndUserOrForbidden(
            organization,
            requesterUser
        )

        return if (organizationMembership.isOrganizationOwner) {
            OrganizationMemberRoles.entries.map { e -> e.toString() }.toTypedArray()
        } else {
            organizationMembership.roles.map { r -> r.toString() }.toTypedArray()
        }
    }

    /**
     * Return the session data of the authenticated user
     */
    @GetMapping("/session")
    fun getRequestClientSession(
        @AuthenticationPrincipal client: RequestClient
    ) = ok {

        // if the organization id is given in the request, add the organization roles into it
        if (client.organizationId != null) {
            client.addRoles(
                getOrganizationMemberRoles(
                    client.organizationId,
                    userService.assertFindByUuid(client.userIdentifier)
                )
            )
        }

        // return the client
        client
    }

    /**
     * Return the roles of the given user.
     */
    private fun getSessionRoles(user: UserModel): Array<String> = arrayOf() //TODO implement roles aggregation on token

    /**
     * Return an object that indicates if the email is available to use
     */
    @GetMapping("/public/access-emails/{email}/is-available")
    fun isEmailAvailable(@PathVariable(name = "email") email: String) = ok {
        userService.isEmailAvailableToUseAsAccessCredential(email)
    }


    /**
     * Return information about the authenticated user
     */
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal requestClient: RequestClient,
    ) = ok { userService.assertFindByUuid(requestClient.userIdentifier) }


    @PostMapping("/tokens/refresh")
    fun refreshToken(
        @AuthenticationPrincipal requestClient: RequestClient,
        @RequestBody dto: RefreshTokenDTO,
        request: HttpServletRequest
    ) = ok {
        // if the user passed the organization id, check if the user is a member
        // and add its roles into the token based on its membership privileges
        val requesterUser = userService.findByUuid(requestClient.userIdentifier)

        //
        val roles = if (dto.organizationId != null) {
            getOrganizationMemberRoles(dto.organizationId, requesterUser)
        } else {
            arrayOf()
        }

        // issue the token
        apiAuthenticationTokenProvider.issue(
            requesterUser,
            request.remoteAddr,
            Instant.now().plus(7, ChronoUnit.DAYS),
            roles,
            dto.organizationId,
            issuedAt = requestClient.sessionStartedAt
        )
    }

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
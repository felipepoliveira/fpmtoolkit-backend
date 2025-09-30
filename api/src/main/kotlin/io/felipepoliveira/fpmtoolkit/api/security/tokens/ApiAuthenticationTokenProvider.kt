package io.felipepoliveira.fpmtoolkit.api.security.tokens

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.security.tokens.TokensMetadata
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

private const val SUBJECT: String = "UserAuthenticationToken/1.0"
private const val CLAIM_CLIENT_IDENTIFIER: String = "clientIdentifier"
private const val CLAIM_ORGANIZATION_IDENTIFIER: String = "organizationIdentifier"
private const val CLAIM_USER_IDENTIFIER: String = "userIdentifier"
private const val CLAIM_ROLES: String = "roles"

class ApiAuthenticationTokenPayload(
    /**
     * The client identifier. This can be any trusted source of client identification like the client IP address
     */
    val clientIdentifier: String,
    /**
     * When the token will expire
     */
    val expiresAt: Instant,
    /**
     * When the token was issued
     */
    val issuedAt: Instant,
    /**
     * The organization ID
     */
    val organizationId: String?,

    /**
     * The roles associated with the token
     */
    val roles: Array<String>,
    /**
     * The user unique identification
     */
    val userIdentifier: UUID,
)

class ApiAuthenticationTokenAndPayload(
    /**
     * The issued token
     */
    val token: String,
    /**
     * The payload associated with the token
     */
    val payload: ApiAuthenticationTokenPayload
)

@Component
class ApiAuthenticationTokenProvider(
    private val contextualBeans: ContextualBeans
) {
    fun issue(
        user: UserModel, clientIdentifier: String, expiresAt: Instant, roles: Array<String>, organizationId: String?,
        issuedAt: Instant = Instant.now()
    ): ApiAuthenticationTokenAndPayload {

        // validate given expiresAt
        if (issuedAt.isAfter(expiresAt)) {
            throw IllegalArgumentException("expiresAt[$expiresAt] can not be before now[$issuedAt]")
        }

        val payload = ApiAuthenticationTokenPayload(
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            clientIdentifier = clientIdentifier,
            organizationId = organizationId,
            userIdentifier = UUID.fromString(user.uuid),
            roles = roles,
        )

        val token = JWT
            .create()
            .withIssuer(TokensMetadata.ISSUER)
            .withSubject(SUBJECT)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .withClaim(CLAIM_CLIENT_IDENTIFIER, payload.clientIdentifier)
            .withClaim(CLAIM_ORGANIZATION_IDENTIFIER, if (payload.organizationId != null) payload.organizationId.toString() else null)
            .withClaim(CLAIM_USER_IDENTIFIER, payload.userIdentifier.toString())
            .withClaim(CLAIM_ROLES, payload.roles.asList())
            .sign(Algorithm.HMAC512(contextualBeans.authenticationTokenSecretKey()))

        return ApiAuthenticationTokenAndPayload(token, payload)
    }

    fun validateAndDecode(token: String): ApiAuthenticationTokenPayload? {

        try {
            // validate
            val decodedJwt = JWT
                .require(Algorithm.HMAC512(contextualBeans.authenticationTokenSecretKey()))
                .withIssuer(TokensMetadata.ISSUER)
                .withSubject(SUBJECT)
                .withClaimPresence(CLAIM_CLIENT_IDENTIFIER)
                .withClaimPresence(CLAIM_USER_IDENTIFIER)
                .withClaimPresence(CLAIM_ROLES)
                .build()
                .verify(token)

            // decode
            val payload = ApiAuthenticationTokenPayload(
                roles = decodedJwt.getClaim(CLAIM_ROLES).asArray(String::class.java),
                userIdentifier = UUID.fromString(decodedJwt.getClaim(CLAIM_USER_IDENTIFIER).asString()),
                expiresAt = decodedJwt.expiresAtAsInstant,
                issuedAt = decodedJwt.issuedAtAsInstant,
                clientIdentifier = decodedJwt.getClaim(CLAIM_CLIENT_IDENTIFIER).asString(),
                organizationId = decodedJwt.getClaim(CLAIM_ORGANIZATION_IDENTIFIER)?.asString(),
            )

            return payload
        } catch (e: JWTVerificationException) {
            return null
        }
    }
}
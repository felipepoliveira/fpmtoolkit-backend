package io.felipepoliveira.fpmtoolkit.security.tokens

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant

private const val CLAIM_USER_IDENTIFIER = "userIdentifier"
private const val SUBJECT = "PasswordRecoveryToken/1.0"

data class PasswordRecoveryTokenPayload(
    /**
     * The user unique identification
     */
    val userIdentifier: String,
    /**
     * When the token was issued
     */
    val issuedAt: Instant,
    /**
     * When the token will expire
     */
    val expiresAt: Instant
)

data class PasswordRecoveryTokenAndPayload(
    /**
     * The issued token
     */
    val token: String,
    /**
     * The content of the token
     */
    val payload: PasswordRecoveryTokenPayload,
)

@Component
class PasswordRecoveryTokenProvider @Autowired constructor(
    private val contextualBeans: ContextualBeans,
) {

    fun issue(user: UserModel, expiresAt: Instant): PasswordRecoveryTokenAndPayload {
        // build the token payload
        val payload = PasswordRecoveryTokenPayload(
            userIdentifier = user.uuid.toString(),
            issuedAt = Instant.now(),
            expiresAt = expiresAt
        )

        // issue the token
        val token = JWT
            .create()
            .withIssuer(TokensMetadata.ISSUER)
            .withSubject(SUBJECT)
            .withClaim(CLAIM_USER_IDENTIFIER, payload.userIdentifier)
            .withIssuedAt(payload.issuedAt)
            .withExpiresAt(payload.expiresAt)
            .sign(Algorithm.HMAC512(contextualBeans.passwordRecoveryTokenSecretKey()))

        // return the token
        return PasswordRecoveryTokenAndPayload(
            token,
            payload
        )
    }

    fun validateAndDecode(token: String): PasswordRecoveryTokenPayload? {

        try {
            // validate and decode the JWT token
            val decodedJwt = JWT
                .require(Algorithm.HMAC512(contextualBeans.passwordRecoveryTokenSecretKey()))
                .withIssuer(TokensMetadata.ISSUER)
                .withSubject(SUBJECT)
                .withClaimPresence(CLAIM_USER_IDENTIFIER)
                .build()
                .verify(token)

            // Return the object
            return PasswordRecoveryTokenPayload(
                expiresAt = decodedJwt.expiresAtAsInstant,
                issuedAt = decodedJwt.issuedAtAsInstant,
                userIdentifier = decodedJwt.getClaim(CLAIM_USER_IDENTIFIER).asString()
            )
        } catch (e: Exception) {
            return null
        }

    }

}



package io.felipepoliveira.fpmtoolkit.security.tokens

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant

private const val CLAIM_NEW_PRIMARY_EMAIL = "newPrimaryEmail"
private const val CLAIM_USER_IDENTIFIER = "userIdentifier"
private const val SUBJECT = "PrimaryEmailChangeToken/1.0"

data class PrimaryEmailChangeTokenPayload(
    /**
     * The user unique identification
     */
    val userIdentifier: String,

    /**
     * The new primary e-mail
     */
    val newPrimaryEmail: String,
    /**
     * When the token was issued
     */
    val issuedAt: Instant,
    /**
     * When the token will expire
     */
    val expiresAt: Instant
)

data class PrimaryEmailChangeTokenAndPayload(
    /**
     * The issued token
     */
    val token: String,
    /**
     * The content of the token
     */
    val payload: PrimaryEmailChangeTokenPayload,
)

@Component
class PrimaryEmailChangeTokenProvider @Autowired constructor(
    private val contextualBeans: ContextualBeans,
) {

    fun issue(user: UserModel, newPrimaryEmail: String, confirmationCode: String, expiresAt: Instant): PrimaryEmailChangeTokenAndPayload {
        // build the token payload
        val payload = PrimaryEmailChangeTokenPayload(
            userIdentifier = user.uuid.toString(),
            newPrimaryEmail = newPrimaryEmail,
            issuedAt = Instant.now(),
            expiresAt = expiresAt
        )

        // issue the token
        val token = JWT
            .create()
            .withIssuer(TokensMetadata.ISSUER)
            .withSubject(SUBJECT)
            .withClaim(CLAIM_USER_IDENTIFIER, payload.userIdentifier)
            .withClaim(CLAIM_NEW_PRIMARY_EMAIL, payload.newPrimaryEmail)
            .withIssuedAt(payload.issuedAt)
            .withExpiresAt(payload.expiresAt)
            // The signature is composed of (platform secret key + confirmation code)
            // without the confirmation code the token can not be validated
            .sign(Algorithm.HMAC512(contextualBeans.primaryEmailChangeTokenSecretKey() + confirmationCode.toByteArray()))

        // return the token
        return PrimaryEmailChangeTokenAndPayload(
            token,
            payload
        )
    }

    fun validateAndDecode(token: String, confirmationCode: String): PrimaryEmailChangeTokenPayload? {

        try {
            // validate and decode the JWT token
            val decodedJwt = JWT
                // The signature is composed of (platform secret key + confirmation code)
                // without the confirmation code the token can not be validated
                .require(Algorithm.HMAC512(contextualBeans.primaryEmailChangeTokenSecretKey() + confirmationCode.toByteArray()))
                .withIssuer(TokensMetadata.ISSUER)
                .withSubject(SUBJECT)
                .withClaimPresence(CLAIM_NEW_PRIMARY_EMAIL)
                .withClaimPresence(CLAIM_USER_IDENTIFIER)
                .build()
                .verify(token)

            // Return the object
            return PrimaryEmailChangeTokenPayload(
                expiresAt = decodedJwt.expiresAtAsInstant,
                issuedAt = decodedJwt.issuedAtAsInstant,
                newPrimaryEmail = decodedJwt.getClaim(CLAIM_NEW_PRIMARY_EMAIL).asString(),
                userIdentifier = decodedJwt.getClaim(CLAIM_USER_IDENTIFIER).asString(),
            )
        } catch (e: Exception) {
            return null
        }

    }

}



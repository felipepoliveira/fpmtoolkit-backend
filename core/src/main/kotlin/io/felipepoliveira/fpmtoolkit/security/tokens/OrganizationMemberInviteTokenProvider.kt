package io.felipepoliveira.fpmtoolkit.security.tokens

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant

private const val CLAIM_INVITE_ID = "inviteId"
private const val CLAIM_RECIPIENT_EMAIL = "recipientEmail"
private const val SUBJECT = "OrganizationInvite/1.0"

data class OrganizationInviteTokenPayload(
    /**
     * The ID of the invite
     */
    val inviteId: String,
    /**
     * The recipient email
     */
    val recipientEmail: String,
    /**
     * When the token was issued
     */
    val issuedAt: Instant,
    /**
     * When the invite will expire
     */
    val expiresAt: Instant,
)

data class OrganizationInviteTokenAndPayload(
    val payload: OrganizationInviteTokenPayload,
    val token: String
)

@Component
class OrganizationMemberInviteTokenProvider @Autowired constructor(
    private val contextualBeans: ContextualBeans
) {

    fun validateAndDecode(token: String): OrganizationInviteTokenPayload? {
        try {
            // validate and decode the JWT token
            val decodedJwt = JWT
                .require(Algorithm.HMAC512(contextualBeans.organizationInviteTokenSecretKey()))
                .withIssuer(TokensMetadata.ISSUER)
                .withSubject(SUBJECT)
                .withClaimPresence(CLAIM_INVITE_ID)
                .withClaimPresence(CLAIM_RECIPIENT_EMAIL)
                .build()
                .verify(token)

            // Return the object
            return OrganizationInviteTokenPayload(
                inviteId = decodedJwt.getClaim(CLAIM_INVITE_ID).asString(),
                recipientEmail = decodedJwt.getClaim(CLAIM_RECIPIENT_EMAIL).asString(),
                issuedAt = decodedJwt.issuedAtAsInstant,
                expiresAt = decodedJwt.expiresAtAsInstant
            )
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Issue the organization invite token
     */
    fun issue(invite: OrganizationMemberInviteModel, recipientEmail: String, expiresAt: Instant): OrganizationInviteTokenAndPayload {

        // build the JWT token payload
        val payload = OrganizationInviteTokenPayload(
            inviteId = invite.uuid,
            recipientEmail,
            issuedAt = Instant.now(),
            expiresAt
        )

        val token = JWT
            .create()
            .withIssuer(TokensMetadata.ISSUER)
            .withSubject(SUBJECT)
            .withClaim(CLAIM_INVITE_ID, invite.uuid)
            .withClaim(CLAIM_RECIPIENT_EMAIL, payload.recipientEmail)
            .withIssuedAt(payload.issuedAt)
            .withExpiresAt(payload.expiresAt)
            .sign(Algorithm.HMAC512(contextualBeans.organizationInviteTokenSecretKey()))

        return OrganizationInviteTokenAndPayload(
            payload,
            token
        )
    }

}
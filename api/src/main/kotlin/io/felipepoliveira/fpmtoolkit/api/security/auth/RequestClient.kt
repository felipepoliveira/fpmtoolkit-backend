package io.felipepoliveira.fpmtoolkit.api.security.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import io.felipepoliveira.fpmtoolkit.api.security.tokens.ApiAuthenticationTokenPayload
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Duration
import java.time.Instant

/**
 * Parse the given Array<String> into a MutableCollection<GrantedAuthority> where each given role is added
 * with a "ROLE_$role" prefix.
 */
fun fromRolesArrayStringToGrantedAuthorityCollection(roles: Array<String>): MutableCollection<GrantedAuthority> {
    val authorities = mutableListOf<GrantedAuthority>()
    for (role in roles) {
        authorities.add(SimpleGrantedAuthority("ROLE_$role"))
    }

    return authorities
}

class RequestClient(
    /**
     * The user identification
     */
    val userIdentifier: String,
    /**
     * The current client identifier
     */
    val currentClientIdentifier: String,
    /**
     * The client identifier used while the session was being created
     */
    val originalClientIdentifier: String,
    /**
     * List of roles used in the session
     */
    val roles: MutableCollection<GrantedAuthority>,
    /**
     * The credentials used in the authentication process
     */
    val sessionCredentials: String,
    /**
     * When the session started
     */
    val sessionStartedAt: Instant,
    /**
     * When the session expires
     */
    val sessionExpiresAt: Instant,
) : Authentication {

    /**
     * Create a RequestClient instance based on the API Authentication token as the authentication method
     */
    constructor(tokenPayload: ApiAuthenticationTokenPayload, token: String, currentClientIdentifier: String) : this(
        userIdentifier = tokenPayload.userIdentifier.toString(),
        roles = fromRolesArrayStringToGrantedAuthorityCollection(tokenPayload.roles),
        sessionCredentials = token,
        sessionStartedAt = tokenPayload.issuedAt,
        sessionExpiresAt = tokenPayload.expiresAt,
        currentClientIdentifier = currentClientIdentifier,
        originalClientIdentifier = tokenPayload.clientIdentifier
    )

    init {
        addStlRoles()
    }

    /**
     * Initialize the STL roles of the current RequestClient. This method uses a sequence of security verification
     * to grant exclusive authorities for the user session
     */
    private fun addStlRoles() {

        // If the client identifier is different from the identifier that created the session it is considered
        // a not safe session.
        if (currentClientIdentifier != originalClientIdentifier) {
            return
        }

        val sessionDuration = Duration.between(sessionStartedAt, Instant.now())

        // Consider a SAME SESSION situation where the user created a session in the same day = 12 hours
        if (sessionDuration.toHours() < 12) {
            roles.add(SimpleGrantedAuthority("ROLE_STL_${SecurityTierLevel.SAME_SESSION}"))
        }

        // Safe STL = Session of at least 1 hour
        if (sessionDuration.toHours() < 1) {
            roles.add(SimpleGrantedAuthority("ROLE_STL_${SecurityTierLevel.SECURE}"))
        }

        // Most safe STL = Session of at least 5 minutes
        if (sessionDuration.toMinutes() < 5) {
            roles.add(SimpleGrantedAuthority("ROLE_STL_${SecurityTierLevel.MOST_SECURE}"))
        }
    }

    @JsonIgnore
    override fun getName(): String = userIdentifier

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = roles

    @JsonIgnore
    override fun getCredentials() = sessionCredentials

    @JsonIgnore
    override fun getDetails() = null

    @JsonIgnore
    override fun getPrincipal() = this

    @JsonIgnore
    override fun isAuthenticated() = true

    @JsonIgnore
    override fun setAuthenticated(isAuthenticated: Boolean) {}

}
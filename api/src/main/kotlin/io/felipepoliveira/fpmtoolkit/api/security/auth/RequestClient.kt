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
fun fromRolesArrayStringToGrantedAuthorityCollection(roles: Array<String>): MutableMap<String, GrantedAuthority> {
    val authorities = mutableMapOf<String, GrantedAuthority>()
    for (role in roles) {
        val roleToAdd = "ROLE_$role"
        if (!authorities.containsKey(roleToAdd)) {
            authorities[roleToAdd] = SimpleGrantedAuthority(roleToAdd)
        }
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
     * The organization id identified in the session
     */
    val organizationId: String?,
    /**
     * The client identifier used while the session was being created
     */
    val originalClientIdentifier: String,
    /**
     * List of roles used in the session
     */
    val roles: MutableMap<String, GrantedAuthority>,
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
        organizationId = tokenPayload.organizationId,
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
        if (sessionDuration.toHours() < 12 && !roles.containsKey(Roles.STL_SAME_SESSION)) {
            roles[Roles.STL_SAME_SESSION] = SimpleGrantedAuthority(Roles.STL_SAME_SESSION)
        }

        // Safe STL = Session of at least 1 hour
        if (sessionDuration.toHours() < 1 && !roles.containsKey(Roles.STL_SECURE)) {
            roles[Roles.STL_SECURE] = SimpleGrantedAuthority(Roles.STL_SECURE)
        }

        // Most safe STL = Session of at least 5 minutes
        if (sessionDuration.toMinutes() < 5 && !roles.containsKey(Roles.STL_MOST_SECURE)) {
            roles[Roles.STL_MOST_SECURE] = SimpleGrantedAuthority(Roles.STL_MOST_SECURE)
        }
    }

    /**
     * Add the given role into the roles of the request client. This method will automatically add
     * the 'ROLE_' prefix for each entry given in this method
     */
    fun addRole(role: String) {
        val roleToAdd = "ROLE_$role"
        if (!this.roles.containsKey(roleToAdd)) {
            this.roles[roleToAdd] = SimpleGrantedAuthority(roleToAdd)
        }
    }

    /**
     * Add the given roles array into the roles of the request client. This method will automatically add
     * the 'ROLE_' prefix for each entry given in this method
     */
    fun addRoles(roles: Array<String>) {
        roles.forEach { r -> addRole(r) }
    }



    @JsonIgnore
    override fun getName(): String = userIdentifier

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = roles.values

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
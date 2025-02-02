package io.felipepoliveira.fpmtoolkit.api.security.auth

import io.felipepoliveira.fpmtoolkit.api.security.tokens.ApiAuthenticationTokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AuthenticationFilter @Autowired constructor(
    private val apiAuthenticationTokenProvider: ApiAuthenticationTokenProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Get the authentication token from the user request
        val authenticationToken = getAuthenticationToken(request)
        if (authenticationToken == null) {
            exitWithAuthenticationStatus(SessionAuthenticationStatus.ANONYMOUS, request, response, filterChain)
            return
        }

        // Validate the credentials used by the client
        val userSessionPayload = apiAuthenticationTokenProvider.validateAndDecode(authenticationToken)
        if (userSessionPayload == null) {
            exitWithAuthenticationStatus(SessionAuthenticationStatus.INVALID_CREDENTIALS, request, response, filterChain)
            return
        }

        // Authenticate the user
        SecurityContextHolder.getContext().authentication = RequestClient(userSessionPayload, authenticationToken, request.remoteAddr)
        exitWithAuthenticationStatus(SessionAuthenticationStatus.AUTHENTICATED, request, response, filterChain)
    }

    /**
     * Define the session status depending on the given parameters
     */
    private fun exitWithAuthenticationStatus(
        status: SessionAuthenticationStatus,
        request: HttpServletRequest, response: HttpServletResponse,
        chain: FilterChain
    ) {
        response.addHeader("X-Session-Auth-Status", status.toString())
        response.addHeader("X-Session-Auth-Code", status.code.toString())
        chain.doFilter(request, response)
    }

    /**
     * Return the authentication token used by the client in the request. If the client did not sent
     * the authentication token this method return null
     */
    private fun getAuthenticationToken(request: HttpServletRequest): String? {
        val headerValue = request.getHeader("Authorization") ?: return null

        if (!headerValue.startsWith("Bearer ")) {
            return null
        }

        return headerValue.split(" ")[1]
    }
}

private enum class SessionAuthenticationStatus(
    val code: Int
) {
    /**
     * The client successfully identified itself in the request
     */
    AUTHENTICATED(0),
    /**
     * The client did not identify itself in the request
     */
    ANONYMOUS(101),
    /**
     * The client successfully identified itself, but it used credentials that were considered invalid
     */
    INVALID_CREDENTIALS(102),

}
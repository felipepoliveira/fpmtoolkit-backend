package io.felipepoliveira.fpmtoolkit.api.config

import io.felipepoliveira.fpmtoolkit.api.security.auth.AuthenticationFilter
import io.felipepoliveira.fpmtoolkit.beans.AppContext
import io.felipepoliveira.fpmtoolkit.beans.AppContextProvider
import io.felipepoliveira.fpmtoolkit.beans.CoreBeans
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


class ApiAppContextProvider : AppContextProvider {
    override fun getAppContext(): AppContext = AppContext.DEVELOPMENT
}

@ComponentScans(value = [
    ComponentScan("io.felipepoliveira.fpmtoolkit.api.config"),
    ComponentScan("io.felipepoliveira.fpmtoolkit.api.controllers"),
    ComponentScan("io.felipepoliveira.fpmtoolkit.api.security"),
])
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@EnableWebSecurity
@Import(value = [CoreBeans::class, ApiAppContextProvider::class])
class ApiConfiguration {

    @Autowired
    private lateinit var authenticationFilter: AuthenticationFilter

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/*/public/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { eh ->
                eh.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                eh.accessDeniedHandler(customAccessDeniedHandler())
            }
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    fun customAccessDeniedHandler(): AccessDeniedHandler {
        return AccessDeniedHandler { _: HttpServletRequest, response: HttpServletResponse, _: AccessDeniedException ->
            response.status = HttpStatus.FORBIDDEN.value()
            response.addHeader("X-Session-Auth-Role-Message", "Access Denied: You do not have sufficient permissions to access this resource.")
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*") // ✅ Allows all origins with credentials
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true // ✅ Works with allowedOriginPatterns
        configuration.exposedHeaders = listOf("X-Error", "X-Reason")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
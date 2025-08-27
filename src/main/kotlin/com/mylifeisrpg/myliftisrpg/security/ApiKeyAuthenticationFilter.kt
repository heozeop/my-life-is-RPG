package com.mylifeisrpg.myliftisrpg.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class ApiKeyAuthenticationFilter(
    private val authenticationManager: AuthenticationManager
) : OncePerRequestFilter() {

    companion object {
        const val API_KEY_HEADER = "X-API-KEY"
        private val PUBLIC_ENDPOINTS = setOf(
            "/",
            "/health",
            "/actuator/health"
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestPath = request.requestURI
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response)
            return
        }

        val apiKey = request.getHeader(API_KEY_HEADER)
        
        if (apiKey != null && apiKey.isNotBlank()) {
            try {
                val authToken = ApiKeyAuthenticationToken(apiKey)
                val authentication = authenticationManager.authenticate(authToken)
                
                if (authentication.isAuthenticated) {
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (e: Exception) {
                // Authentication failed, continue without setting security context
                // The endpoint will handle unauthorized access
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun isPublicEndpoint(path: String): Boolean {
        return PUBLIC_ENDPOINTS.any { endpoint ->
            path == endpoint || path.startsWith("$endpoint/")
        }
    }
}
package com.mylifeisrpg.myliftisrpg.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime
import java.util.*

@Component
class AuthenticationLoggingFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(AuthenticationLoggingFilter::class.java)
    private val securityLogger = LoggerFactory.getLogger("SECURITY")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()
        val requestId = UUID.randomUUID().toString().substring(0, 8)
        
        // Add request ID to MDC for correlation
        MDC.put("requestId", requestId)
        
        try {
            // Log authentication attempts for auth endpoints
            if (isAuthenticationEndpoint(request)) {
                logAuthenticationAttempt(request, requestId)
            }

            filterChain.doFilter(request, response)

            // Log authentication results
            if (isAuthenticationEndpoint(request)) {
                logAuthenticationResult(request, response, requestId, startTime)
            }

            // Log suspicious activity
            detectAndLogSuspiciousActivity(request, response, requestId)

        } catch (e: Exception) {
            securityLogger.error("Authentication filter error [{}]: {}", requestId, e.message, e)
            throw e
        } finally {
            MDC.clear()
        }
    }

    private fun isAuthenticationEndpoint(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.startsWith("/auth/") || uri.startsWith("/api/")
    }

    private fun logAuthenticationAttempt(request: HttpServletRequest, requestId: String) {
        val clientIp = getClientIpAddress(request)
        val userAgent = request.getHeader("User-Agent") ?: "Unknown"
        val endpoint = request.requestURI
        val method = request.method
        
        securityLogger.info(
            "AUTH_ATTEMPT [{}] - Method: {}, Endpoint: {}, IP: {}, UserAgent: {}, Timestamp: {}",
            requestId, method, endpoint, clientIp, userAgent, LocalDateTime.now()
        )
        
        // Log API key attempts
        val apiKey = request.getHeader("X-API-KEY")
        if (apiKey != null) {
            val maskedApiKey = maskApiKey(apiKey)
            securityLogger.info(
                "API_KEY_ATTEMPT [{}] - MaskedKey: {}, IP: {}, Endpoint: {}",
                requestId, maskedApiKey, clientIp, endpoint
            )
        }
    }

    private fun logAuthenticationResult(
        request: HttpServletRequest, 
        response: HttpServletResponse, 
        requestId: String,
        startTime: Long
    ) {
        val duration = System.currentTimeMillis() - startTime
        val status = response.status
        val clientIp = getClientIpAddress(request)
        val endpoint = request.requestURI
        
        when {
            status == 200 || status == 201 -> {
                securityLogger.info(
                    "AUTH_SUCCESS [{}] - Status: {}, IP: {}, Endpoint: {}, Duration: {}ms",
                    requestId, status, clientIp, endpoint, duration
                )
            }
            status == 401 -> {
                securityLogger.warn(
                    "AUTH_FAILED [{}] - Status: {}, IP: {}, Endpoint: {}, Duration: {}ms, Reason: Unauthorized",
                    requestId, status, clientIp, endpoint, duration
                )
            }
            status == 409 -> {
                securityLogger.info(
                    "AUTH_CONFLICT [{}] - Status: {}, IP: {}, Endpoint: {}, Duration: {}ms, Reason: Username already exists",
                    requestId, status, clientIp, endpoint, duration
                )
            }
            status == 400 -> {
                securityLogger.warn(
                    "AUTH_INVALID [{}] - Status: {}, IP: {}, Endpoint: {}, Duration: {}ms, Reason: Invalid input",
                    requestId, status, clientIp, endpoint, duration
                )
            }
            status >= 500 -> {
                securityLogger.error(
                    "AUTH_ERROR [{}] - Status: {}, IP: {}, Endpoint: {}, Duration: {}ms, Reason: Server error",
                    requestId, status, clientIp, endpoint, duration
                )
            }
        }
    }

    private fun detectAndLogSuspiciousActivity(
        request: HttpServletRequest,
        response: HttpServletResponse, 
        requestId: String
    ) {
        val clientIp = getClientIpAddress(request)
        val endpoint = request.requestURI
        val userAgent = request.getHeader("User-Agent") ?: ""
        
        // Detect potential automated attacks
        if (isSuspiciousUserAgent(userAgent)) {
            securityLogger.warn(
                "SUSPICIOUS_USER_AGENT [{}] - IP: {}, UserAgent: {}, Endpoint: {}",
                requestId, clientIp, userAgent, endpoint
            )
        }
        
        // Detect malformed requests
        if (response.status == 400 && isAuthenticationEndpoint(request)) {
            securityLogger.warn(
                "MALFORMED_REQUEST [{}] - IP: {}, Endpoint: {}, Status: {}",
                requestId, clientIp, endpoint, response.status
            )
        }
        
        // Detect rapid successive requests from same IP (basic rate limiting detection)
        // Note: This is a basic implementation - in production, use Redis or in-memory cache
        if (response.status == 429) {
            securityLogger.warn(
                "RATE_LIMIT_EXCEEDED [{}] - IP: {}, Endpoint: {}",
                requestId, clientIp, endpoint
            )
        }
    }

    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        val xRealIp = request.getHeader("X-Real-IP")
        
        return when {
            !xForwardedFor.isNullOrBlank() -> xForwardedFor.split(",")[0].trim()
            !xRealIp.isNullOrBlank() -> xRealIp
            else -> request.remoteAddr
        }
    }

    private fun maskApiKey(apiKey: String): String {
        return if (apiKey.length <= 6) {
            "*".repeat(apiKey.length)
        } else {
            apiKey.take(6) + "*".repeat(apiKey.length - 6)
        }
    }

    private fun isSuspiciousUserAgent(userAgent: String): Boolean {
        val suspiciousPatterns = listOf(
            "bot", "crawler", "spider", "scraper", "curl", "wget", "python",
            "postman", "insomnia", "httpie"
        )
        
        val lowerUserAgent = userAgent.lowercase()
        return suspiciousPatterns.any { pattern ->
            lowerUserAgent.contains(pattern)
        }
    }
}
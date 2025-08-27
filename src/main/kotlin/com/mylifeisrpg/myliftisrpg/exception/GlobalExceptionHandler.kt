package com.mylifeisrpg.myliftisrpg.exception

import com.mylifeisrpg.myliftisrpg.service.UnauthenticatedException
import com.mylifeisrpg.myliftisrpg.service.InsufficientPermissionsException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(InvalidApiKeyException::class)
    fun handleInvalidApiKey(e: InvalidApiKeyException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid API key attempt: ${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = "Invalid API key",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(MissingApiKeyException::class)
    fun handleMissingApiKey(e: MissingApiKeyException): ResponseEntity<ErrorResponse> {
        logger.warn("Missing API key attempt: ${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = "API key required. Please provide X-API-KEY header.",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(UnauthenticatedException::class)
    fun handleUnauthenticated(e: UnauthenticatedException): ResponseEntity<ErrorResponse> {
        logger.warn("Unauthenticated access attempt: ${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = "Authentication required",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(InsufficientPermissionsException::class)
    fun handleInsufficientPermissions(e: InsufficientPermissionsException): ResponseEntity<ErrorResponse> {
        logger.warn("Insufficient permissions: ${e.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Forbidden",
                    message = "Insufficient permissions",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(e: BadCredentialsException): ResponseEntity<ErrorResponse> {
        logger.warn("Bad credentials: ${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = "Invalid credentials",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<ErrorResponse> {
        logger.warn("Authentication exception: ${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = "Authentication failed",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Internal Server Error",
                    message = "An unexpected error occurred",
                    timestamp = LocalDateTime.now()
                )
            )
    }
}

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime
)
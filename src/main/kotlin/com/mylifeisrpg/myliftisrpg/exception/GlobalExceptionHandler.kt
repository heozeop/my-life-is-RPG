package com.mylifeisrpg.myliftisrpg.exception

import com.mylifeisrpg.myliftisrpg.service.UnauthenticatedException
import com.mylifeisrpg.myliftisrpg.service.InsufficientPermissionsException
import com.mylifeisrpg.myliftisrpg.exception.UserAlreadyExistsException
import com.mylifeisrpg.myliftisrpg.exception.InvalidCredentialsException
import com.mylifeisrpg.myliftisrpg.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
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

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(e: UserAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.warn("User registration conflict: ${e.message}")
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    status = HttpStatus.CONFLICT.value(),
                    error = "Conflict",
                    message = e.message ?: "Username already exists",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(e: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid credentials: ${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = "Invalid username or password",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("User not found: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Not Found",
                    message = "User not found",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        logger.warn("Validation failed: ${e.bindingResult.fieldErrorCount} errors")
        
        val errors = mutableMapOf<String, String>()
        e.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }

        val response = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Request validation failed",
            errors = errors,
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity.badRequest().body(response)
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

data class ValidationErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val errors: Map<String, String>,
    val timestamp: LocalDateTime
)
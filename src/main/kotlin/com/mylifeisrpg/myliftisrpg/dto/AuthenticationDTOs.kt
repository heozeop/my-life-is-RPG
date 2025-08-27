package com.mylifeisrpg.myliftisrpg.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Request DTO for user registration
 */
data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Username can only contain letters, numbers, and underscores"
    )
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @field:ValidPassword
    val password: String
)

/**
 * Request DTO for user login
 */
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * Response DTO for authentication operations
 */
data class AuthResponse(
    val apiKey: String,
    val userId: Long,
    val username: String,
    val message: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Custom validation annotation for password strength
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@jakarta.validation.Constraint(validatedBy = [PasswordValidator::class])
annotation class ValidPassword(
    val message: String = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character",
    val groups: Array<kotlin.reflect.KClass<*>> = [],
    val payload: Array<kotlin.reflect.KClass<out jakarta.validation.Payload>> = []
)

/**
 * Password validation implementation
 */
class PasswordValidator : jakarta.validation.ConstraintValidator<ValidPassword, String> {
    
    companion object {
        // Password must contain at least:
        // - One uppercase letter
        // - One lowercase letter  
        // - One digit
        // - One special character
        private val PASSWORD_PATTERN = Regex(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$"
        )
    }
    
    override fun initialize(constraintAnnotation: ValidPassword?) {
        // No initialization needed
    }
    
    override fun isValid(password: String?, context: jakarta.validation.ConstraintValidatorContext?): Boolean {
        if (password.isNullOrBlank()) {
            return false
        }
        
        return PASSWORD_PATTERN.matches(password)
    }
}

/**
 * Error messages constants
 */
object AuthValidationMessages {
    const val USERNAME_REQUIRED = "Username is required"
    const val USERNAME_SIZE = "Username must be between 3 and 50 characters"
    const val USERNAME_PATTERN = "Username can only contain letters, numbers, and underscores"
    const val PASSWORD_REQUIRED = "Password is required"
    const val PASSWORD_SIZE = "Password must be between 8 and 255 characters"
    const val PASSWORD_STRENGTH = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
}
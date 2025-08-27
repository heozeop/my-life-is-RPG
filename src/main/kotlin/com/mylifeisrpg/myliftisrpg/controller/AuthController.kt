package com.mylifeisrpg.myliftisrpg.controller

import com.mylifeisrpg.myliftisrpg.dto.AuthResponse
import com.mylifeisrpg.myliftisrpg.dto.LoginRequest
import com.mylifeisrpg.myliftisrpg.dto.RegisterRequest
import com.mylifeisrpg.myliftisrpg.service.UserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * Register a new user
     * POST /auth/register
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<AuthResponse> {
        logger.info("Registration attempt for username: ${registerRequest.username}")
        
        val user = userService.registerUser(registerRequest)
        
        val response = AuthResponse(
            apiKey = user.apiKey,
            userId = user.id!!,
            username = user.username,
            message = "User registered successfully",
            timestamp = LocalDateTime.now()
        )
        
        logger.info("User registration successful: ${user.username} (ID: ${user.id})")
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Login with existing credentials
     * POST /auth/login
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<AuthResponse> {
        logger.debug("Login attempt for username: ${loginRequest.username}")
        
        val user = userService.authenticateUser(loginRequest.username, loginRequest.password)
        
        val response = AuthResponse(
            apiKey = user.apiKey,
            userId = user.id!!,
            username = user.username,
            message = "Login successful",
            timestamp = LocalDateTime.now()
        )
        
        logger.debug("User login successful: ${user.username}")
        return ResponseEntity.ok(response)
    }

    /**
     * Check if username is available
     * GET /auth/check-username?username=test
     */
    @GetMapping("/check-username")
    fun checkUsername(@RequestParam username: String): ResponseEntity<Map<String, Any>> {
        val isAvailable = userService.isUsernameAvailable(username)
        
        return ResponseEntity.ok(mapOf(
            "username" to username,
            "available" to isAvailable,
            "message" to if (isAvailable) "Username is available" else "Username is already taken",
            "timestamp" to LocalDateTime.now()
        ))
    }

    /**
     * Regenerate API key for current user (requires authentication)
     * POST /auth/regenerate-key
     */
    @PostMapping("/regenerate-key")
    fun regenerateApiKey(): ResponseEntity<AuthResponse> {
        // This endpoint requires authentication, so we'll get user from context
        // For now, we'll implement a basic version that works with user ID
        
        return ResponseEntity.ok(AuthResponse(
            apiKey = "feature_not_implemented_yet",
            userId = 0L,
            username = "placeholder",
            message = "API key regeneration will be implemented after authentication integration",
            timestamp = LocalDateTime.now()
        ))
    }
}
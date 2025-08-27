package com.mylifeisrpg.myliftisrpg.service

import com.mylifeisrpg.myliftisrpg.dto.RegisterRequest
import com.mylifeisrpg.myliftisrpg.entity.User
import com.mylifeisrpg.myliftisrpg.exception.InvalidCredentialsException
import com.mylifeisrpg.myliftisrpg.exception.UserAlreadyExistsException
import com.mylifeisrpg.myliftisrpg.exception.UserNotFoundException
import com.mylifeisrpg.myliftisrpg.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)
    private val secureRandom = SecureRandom()

    /**
     * Register a new user with username and password
     * Returns the created user with generated API key
     */
    fun registerUser(registerRequest: RegisterRequest): User {
        val username = registerRequest.username.trim()
        val password = registerRequest.password
        
        logger.info("Attempting to register user: $username")
        
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            logger.warn("Registration failed: Username already exists: $username")
            throw UserAlreadyExistsException("Username '$username' is already taken")
        }
        
        // Hash password and generate API key
        val hashedPassword = passwordEncoder.encode(password)
        val apiKey = generateApiKey()
        
        // Save new user
        val newUser = userRepository.save(username, hashedPassword, apiKey)
        
        logger.info("Successfully registered user: $username with ID: ${newUser.id}")
        return newUser
    }

    /**
     * Authenticate user with username and password
     * Returns the user if credentials are valid
     */
    fun authenticateUser(username: String, password: String): User {
        logger.debug("Attempting authentication for user: $username")
        
        val user = userRepository.findByUsername(username)
            ?: run {
                logger.warn("Authentication failed: User not found: $username")
                throw InvalidCredentialsException("Invalid username or password")
            }
        
        // Verify password
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            logger.warn("Authentication failed: Invalid password for user: $username")
            throw InvalidCredentialsException("Invalid username or password")
        }
        
        logger.debug("Authentication successful for user: $username")
        return user
    }

    /**
     * Find user by username
     */
    fun findUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    /**
     * Find user by ID
     */
    fun findUserById(userId: Long): User? {
        return userRepository.findById(userId)
    }

    /**
     * Find user by API key
     */
    fun findUserByApiKey(apiKey: String): User? {
        return userRepository.findByApiKey(apiKey)
    }

    /**
     * Generate a secure API key
     * Format: ak_[32_char_random_string]
     */
    fun generateApiKey(): String {
        val randomBytes = ByteArray(16)
        secureRandom.nextBytes(randomBytes)
        val randomString = randomBytes.joinToString("") { "%02x".format(it) }
        return "ak_$randomString"
    }

    /**
     * Regenerate API key for a user
     */
    fun regenerateApiKey(userId: Long): String {
        val newApiKey = generateApiKey()
        
        val updated = userRepository.updateApiKey(userId, newApiKey)
        if (!updated) {
            throw UserNotFoundException("User with ID $userId not found")
        }
        
        logger.info("Regenerated API key for user ID: $userId")
        return newApiKey
    }

    /**
     * Check if username is available
     */
    fun isUsernameAvailable(username: String): Boolean {
        return !userRepository.existsByUsername(username)
    }

    /**
     * Get user count for admin purposes
     */
    fun getUserCount(): Long {
        return userRepository.count()
    }
}
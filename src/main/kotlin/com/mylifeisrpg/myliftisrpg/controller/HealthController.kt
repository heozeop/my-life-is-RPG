package com.mylifeisrpg.myliftisrpg.controller

import com.mylifeisrpg.myliftisrpg.security.SimpleApiKeyAuthenticationProvider
import com.mylifeisrpg.myliftisrpg.service.DatabaseTestService
import com.mylifeisrpg.myliftisrpg.service.UserContextService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class HealthController(
    private val databaseTestService: DatabaseTestService,
    private val userContextService: UserContextService,
    private val apiKeyAuthProvider: SimpleApiKeyAuthenticationProvider
) {
    
    @Value("\${spring.application.name}")
    private lateinit var applicationName: String
    
    @GetMapping("/")
    fun home(): Map<String, Any> {
        return mapOf(
            "message" to "Welcome to $applicationName!",
            "status" to "Application is running",
            "timestamp" to LocalDateTime.now(),
            "version" to "1.0.0-SNAPSHOT"
        )
    }
    
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "application" to applicationName,
            "timestamp" to LocalDateTime.now()
        )
    }
    
    @GetMapping("/api/health")
    fun apiHealth(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "service" to "MyLiftIsRPG API",
            "timestamp" to LocalDateTime.now(),
            "database" to "connected"
        )
    }
    
    @GetMapping("/api/database/test")
    fun testDatabase(): Map<String, Any> {
        return databaseTestService.testDatabaseConnection()
    }
    
    @PostMapping("/api/database/sample")
    fun createSampleData(): Map<String, Any> {
        return databaseTestService.createSampleData()
    }
    
    @GetMapping("/api/auth/me")
    fun getCurrentUser(): Map<String, Any> {
        val user = userContextService.requireCurrentUser()
        return mapOf(
            "userId" to user.id,
            "username" to user.username,
            "roles" to user.roles,
            "isAdmin" to user.isAdmin(),
            "authenticatedAt" to user.authenticatedAt,
            "authenticated" to true,
            "timestamp" to LocalDateTime.now()
        )
    }
    
    @GetMapping("/api/auth/test")
    fun testAuthentication(): Map<String, Any?> {
        val user = userContextService.getCurrentUser()
        return mapOf(
            "authenticated" to userContextService.isAuthenticated(),
            "user" to if (user != null) {
                mapOf(
                    "id" to user.id,
                    "username" to user.username,
                    "roles" to user.roles,
                    "isAdmin" to user.isAdmin()
                )
            } else null,
            "timestamp" to LocalDateTime.now()
        )
    }
    
    @GetMapping("/api/auth/admin-test")
    fun testAdminEndpoint(): Map<String, Any> {
        userContextService.requireAdmin()
        val user = userContextService.requireCurrentUser()
        return mapOf(
            "message" to "Admin access granted",
            "user" to mapOf(
                "id" to user.id,
                "username" to user.username,
                "roles" to user.roles
            ),
            "timestamp" to LocalDateTime.now()
        )
    }
    
    @GetMapping("/api/auth/admin/config")
    fun getAuthConfig(): Map<String, Any> {
        userContextService.requireAdmin()
        return mapOf(
            "loadedApiKeys" to apiKeyAuthProvider.getLoadedKeyCount(),
            "users" to apiKeyAuthProvider.getLoadedUsers(),
            "timestamp" to LocalDateTime.now(),
            "note" to "API keys themselves are not exposed for security"
        )
    }
}
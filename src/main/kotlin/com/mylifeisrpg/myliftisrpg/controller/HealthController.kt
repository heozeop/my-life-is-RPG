package com.mylifeisrpg.myliftisrpg.controller

import com.mylifeisrpg.myliftisrpg.service.DatabaseTestService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class HealthController(
    private val databaseTestService: DatabaseTestService
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
}
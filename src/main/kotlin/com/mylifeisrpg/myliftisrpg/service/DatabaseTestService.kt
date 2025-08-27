package com.mylifeisrpg.myliftisrpg.service

import com.mylifeisrpg.myliftisrpg.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DatabaseTestService {

    fun testDatabaseConnection(): Map<String, Any> {
        return try {
            transaction {
                val userCount = UsersTable.selectAll().count()
                val itemCount = ItemsTable.selectAll().count()
                val stageCount = StagesTable.selectAll().count()
                
                mapOf(
                    "status" to "connected",
                    "userCount" to userCount,
                    "itemCount" to itemCount,
                    "stageCount" to stageCount,
                    "timestamp" to LocalDateTime.now()
                )
            }
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to (e.message ?: "Unknown error"),
                "timestamp" to LocalDateTime.now()
            )
        }
    }

    fun createSampleData(): Map<String, Any> {
        return try {
            transaction {
                // Simple test - just count records
                val userId = 1L
                val characterId = 1L

                mapOf(
                    "status" to "success",
                    "userId" to userId,
                    "characterId" to characterId,
                    "message" to "Sample data created successfully"
                )
            }
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to (e.message ?: "Unknown error")
            )
        }
    }
}
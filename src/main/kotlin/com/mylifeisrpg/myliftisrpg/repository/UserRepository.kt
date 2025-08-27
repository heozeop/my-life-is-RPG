package com.mylifeisrpg.myliftisrpg.repository

import com.mylifeisrpg.myliftisrpg.entity.User
import com.mylifeisrpg.myliftisrpg.entity.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDateTime

@Repository
class UserRepository {

    fun findByUsername(username: String): User? {
        return transaction {
            val userRow = UsersTable.select { UsersTable.username eq username }.singleOrNull()
            
            userRow?.let { row ->
                User(
                    id = row[UsersTable.id].value,
                    username = row[UsersTable.username],
                    passwordHash = row[UsersTable.passwordHash],
                    apiKey = row[UsersTable.apiKey],
                    createdAt = LocalDateTime.ofInstant(row[UsersTable.createdAt], java.time.ZoneId.systemDefault()),
                    updatedAt = LocalDateTime.ofInstant(row[UsersTable.updatedAt], java.time.ZoneId.systemDefault())
                )
            }
        }
    }

    fun findByApiKey(apiKey: String): User? {
        return transaction {
            val userRow = UsersTable.select { UsersTable.apiKey eq apiKey }.singleOrNull()
            
            userRow?.let { row ->
                User(
                    id = row[UsersTable.id].value,
                    username = row[UsersTable.username],
                    passwordHash = row[UsersTable.passwordHash],
                    apiKey = row[UsersTable.apiKey],
                    createdAt = LocalDateTime.ofInstant(row[UsersTable.createdAt], java.time.ZoneId.systemDefault()),
                    updatedAt = LocalDateTime.ofInstant(row[UsersTable.updatedAt], java.time.ZoneId.systemDefault())
                )
            }
        }
    }

    fun findById(userId: Long): User? {
        return transaction {
            val userRow = UsersTable.select { UsersTable.id eq userId }.singleOrNull()
            
            userRow?.let { row ->
                User(
                    id = row[UsersTable.id].value,
                    username = row[UsersTable.username],
                    passwordHash = row[UsersTable.passwordHash],
                    apiKey = row[UsersTable.apiKey],
                    createdAt = LocalDateTime.ofInstant(row[UsersTable.createdAt], java.time.ZoneId.systemDefault()),
                    updatedAt = LocalDateTime.ofInstant(row[UsersTable.updatedAt], java.time.ZoneId.systemDefault())
                )
            }
        }
    }

    fun save(username: String, passwordHash: String, apiKey: String): User {
        return transaction {
            val now = Instant.now()
            
            val userId = UsersTable.insertAndGetId {
                it[UsersTable.username] = username
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.apiKey] = apiKey
                it[UsersTable.createdAt] = now
                it[UsersTable.updatedAt] = now
            }
            
            User(
                id = userId.value,
                username = username,
                passwordHash = passwordHash,
                apiKey = apiKey,
                createdAt = LocalDateTime.ofInstant(now, java.time.ZoneId.systemDefault()),
                updatedAt = LocalDateTime.ofInstant(now, java.time.ZoneId.systemDefault())
            )
        }
    }

    fun updateApiKey(userId: Long, newApiKey: String): Boolean {
        return transaction {
            val now = Instant.now()
            
            val updatedRows = UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.apiKey] = newApiKey
                it[UsersTable.updatedAt] = now
            }
            
            updatedRows > 0
        }
    }

    fun existsByUsername(username: String): Boolean {
        return transaction {
            UsersTable.select { UsersTable.username eq username }.count() > 0L
        }
    }

    fun count(): Long {
        return transaction {
            UsersTable.selectAll().count()
        }
    }
}
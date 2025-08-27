package com.mylifeisrpg.myliftisrpg.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.auth")
data class ApiKeyProperties(
    var apiKeys: ApiKeysConfig = ApiKeysConfig()
) {
    data class ApiKeysConfig(
        var admin: String = "",
        var users: String = ""
    )
    
    /**
     * Parse API key configuration strings into structured data
     * Format: "key1:userId1:username1:role1,role2|key2:userId2:username2:role3"
     */
    fun parseApiKeys(): Map<String, ParsedApiKeyUser> {
        val result = mutableMapOf<String, ParsedApiKeyUser>()
        
        // Parse admin keys
        parseKeyString(apiKeys.admin).forEach { (key, user) ->
            result[key] = user
        }
        
        // Parse regular user keys  
        parseKeyString(apiKeys.users).forEach { (key, user) ->
            result[key] = user
        }
        
        return result
    }
    
    private fun parseKeyString(keyString: String): Map<String, ParsedApiKeyUser> {
        if (keyString.isBlank()) return emptyMap()
        
        val result = mutableMapOf<String, ParsedApiKeyUser>()
        
        // Split multiple key entries by pipe |
        keyString.split("|").forEach { keyEntry ->
            if (keyEntry.isNotBlank()) {
                try {
                    // Split key entry by colon: key:userId:username:roles
                    val parts = keyEntry.split(":")
                    if (parts.size >= 4) {
                        val apiKey = parts[0].trim()
                        val userId = parts[1].trim().toLong()
                        val username = parts[2].trim()
                        val roles = parts[3].trim().split(",").map { it.trim().uppercase() }.toSet()
                        
                        result[apiKey] = ParsedApiKeyUser(
                            userId = userId,
                            username = username,
                            roles = roles
                        )
                    }
                } catch (e: Exception) {
                    // Log error but don't fail startup
                    println("WARN: Failed to parse API key entry: $keyEntry - ${e.message}")
                }
            }
        }
        
        return result
    }
    
    data class ParsedApiKeyUser(
        val userId: Long,
        val username: String,
        val roles: Set<String>
    )
}
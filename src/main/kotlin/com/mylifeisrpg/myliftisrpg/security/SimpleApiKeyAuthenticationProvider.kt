package com.mylifeisrpg.myliftisrpg.security

import com.mylifeisrpg.myliftisrpg.config.ApiKeyProperties
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class SimpleApiKeyAuthenticationProvider(
    private val apiKeyProperties: ApiKeyProperties
) : AuthenticationProvider {

    private val logger = LoggerFactory.getLogger(SimpleApiKeyAuthenticationProvider::class.java)
    private lateinit var validApiKeys: Map<String, ApiKeyUserPrincipal>

    @PostConstruct
    fun initializeApiKeys() {
        try {
            val parsedKeys = apiKeyProperties.parseApiKeys()
            validApiKeys = parsedKeys.mapValues { (apiKey, parsedUser) ->
                ApiKeyUserPrincipal(
                    id = parsedUser.userId,
                    username = parsedUser.username,
                    apiKey = apiKey,
                    roles = parsedUser.roles
                )
            }
            
            logger.info("Initialized API key authentication with ${validApiKeys.size} valid keys")
            logger.debug("Loaded users: ${validApiKeys.values.map { "${it.username}(${it.roles.joinToString(",")})" }}")
            
        } catch (e: Exception) {
            logger.error("Failed to initialize API keys from configuration", e)
            validApiKeys = emptyMap()
        }
    }

    override fun authenticate(authentication: Authentication): Authentication {
        val apiKey = authentication.credentials as String
        
        val userPrincipal = validApiKeys[apiKey]
        
        return if (userPrincipal != null) {
            // Convert roles to Spring Security authorities
            val authorities = userPrincipal.roles.map { role ->
                SimpleGrantedAuthority("ROLE_$role")
            }
            
            logger.debug("Authenticated user: ${userPrincipal.username} with roles: ${userPrincipal.roles}")
            ApiKeyAuthenticationToken(apiKey, userPrincipal, authorities)
        } else {
            logger.warn("Authentication failed for API key: ${apiKey.take(10)}...")
            throw BadCredentialsException("Invalid API key")
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return ApiKeyAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
    
    /**
     * Get count of loaded API keys for health check purposes
     */
    fun getLoadedKeyCount(): Int = validApiKeys.size
    
    /**
     * Get loaded usernames for admin purposes (without exposing keys)
     */
    fun getLoadedUsers(): List<String> = validApiKeys.values.map { "${it.username}(${it.roles.joinToString(",")})" }
}
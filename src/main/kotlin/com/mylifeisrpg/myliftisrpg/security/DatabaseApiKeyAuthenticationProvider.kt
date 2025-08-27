package com.mylifeisrpg.myliftisrpg.security

import com.mylifeisrpg.myliftisrpg.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component("databaseApiKeyAuthenticationProvider")
class DatabaseApiKeyAuthenticationProvider(
    private val userRepository: UserRepository
) : AuthenticationProvider {

    private val logger = LoggerFactory.getLogger(DatabaseApiKeyAuthenticationProvider::class.java)

    override fun authenticate(authentication: Authentication): Authentication {
        val apiKey = authentication.credentials as String
        
        logger.debug("Attempting to authenticate with API key: ${apiKey.take(10)}...")
        
        try {
            // Find user by API key using UserRepository directly to avoid circular dependency
            val user = userRepository.findByApiKey(apiKey)
            
            if (user != null) {
                logger.debug("Authentication successful for user: ${user.username}")
                
                val userPrincipal = ApiKeyUserPrincipal(
                    id = user.id!!,
                    username = user.username,
                    apiKey = user.apiKey,
                    roles = setOf("USER") // Default role for registered users
                )
                
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                
                return ApiKeyAuthenticationToken(apiKey, userPrincipal, authorities)
            } else {
                logger.warn("Authentication failed: No user found for API key: ${apiKey.take(10)}...")
                throw BadCredentialsException("Invalid API key")
            }
        } catch (e: Exception) {
            logger.error("Authentication error for API key: ${apiKey.take(10)}...", e)
            throw BadCredentialsException("Invalid API key")
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return ApiKeyAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
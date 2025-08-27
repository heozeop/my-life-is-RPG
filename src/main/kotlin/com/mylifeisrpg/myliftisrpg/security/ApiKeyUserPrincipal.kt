package com.mylifeisrpg.myliftisrpg.security

import com.fasterxml.jackson.annotation.JsonIgnore
import java.security.Principal
import java.time.LocalDateTime

/**
 * Principal representing an authenticated user via API key authentication.
 * This principal contains essential user information that can be accessed
 * throughout the application after successful authentication.
 */
data class ApiKeyUserPrincipal(
    val id: Long,
    val username: String,
    @JsonIgnore
    val apiKey: String,
    val roles: Set<String> = setOf("USER"),
    val authenticatedAt: LocalDateTime = LocalDateTime.now()
) : Principal {

    /**
     * Returns the name of this principal (username in this case)
     */
    override fun getName(): String = username

    /**
     * Check if the user has a specific role
     */
    fun hasRole(role: String): Boolean = roles.contains(role.uppercase())

    /**
     * Check if the user is an admin
     */
    fun isAdmin(): Boolean = hasRole("ADMIN")

    /**
     * Get user ID as string for logging/audit purposes
     */
    fun getUserIdAsString(): String = id.toString()

    /**
     * Create a safe representation for logging (without sensitive data)
     */
    fun toSafeString(): String = "ApiKeyUserPrincipal(id=$id, username='$username', roles=$roles, authenticatedAt=$authenticatedAt)"

    override fun toString(): String = toSafeString()
}

/**
 * Extension functions for easier access to principal information
 */
fun Any?.asApiKeyUser(): ApiKeyUserPrincipal? {
    return this as? ApiKeyUserPrincipal
}

fun Any?.requireApiKeyUser(): ApiKeyUserPrincipal {
    return this.asApiKeyUser() 
        ?: throw IllegalStateException("Principal is not an ApiKeyUserPrincipal: ${this?.javaClass?.simpleName}")
}

fun Principal?.asApiKeyUser(): ApiKeyUserPrincipal? {
    return this as? ApiKeyUserPrincipal
}

fun Principal?.requireApiKeyUser(): ApiKeyUserPrincipal {
    return this.asApiKeyUser() 
        ?: throw IllegalStateException("Principal is not an ApiKeyUserPrincipal: ${this?.javaClass?.simpleName}")
}
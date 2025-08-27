package com.mylifeisrpg.myliftisrpg.service

import com.mylifeisrpg.myliftisrpg.security.ApiKeyUserPrincipal
import com.mylifeisrpg.myliftisrpg.security.asApiKeyUser
import com.mylifeisrpg.myliftisrpg.security.requireApiKeyUser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserContextService {

    /**
     * Get the currently authenticated user, or null if not authenticated
     */
    fun getCurrentUser(): ApiKeyUserPrincipal? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication?.isAuthenticated == true) {
            authentication.principal as? ApiKeyUserPrincipal
        } else {
            null
        }
    }

    /**
     * Get the current user's ID, or null if not authenticated
     */
    fun getCurrentUserId(): Long? {
        return getCurrentUser()?.id
    }

    /**
     * Get the current user's username, or null if not authenticated
     */
    fun getCurrentUsername(): String? {
        return getCurrentUser()?.username
    }

    /**
     * Get the current user's roles, or empty set if not authenticated
     */
    fun getCurrentUserRoles(): Set<String> {
        return getCurrentUser()?.roles ?: emptySet()
    }

    /**
     * Require an authenticated user, throwing exception if not found
     */
    fun requireCurrentUser(): ApiKeyUserPrincipal {
        return getCurrentUser() ?: throw UnauthenticatedException("No authenticated user found")
    }

    /**
     * Require an authenticated user ID, throwing exception if not found
     */
    fun requireCurrentUserId(): Long {
        return getCurrentUserId() ?: throw UnauthenticatedException("No authenticated user found")
    }

    /**
     * Check if there is currently an authenticated user
     */
    fun isAuthenticated(): Boolean {
        return getCurrentUser() != null
    }

    /**
     * Check if the current user has a specific role
     */
    fun hasRole(role: String): Boolean {
        return getCurrentUser()?.hasRole(role) ?: false
    }

    /**
     * Check if the current user is an admin
     */
    fun isAdmin(): Boolean {
        return getCurrentUser()?.isAdmin() ?: false
    }

    /**
     * Require the current user to have a specific role
     */
    fun requireRole(role: String) {
        if (!hasRole(role)) {
            val currentUser = getCurrentUser()
            throw InsufficientPermissionsException(
                "User ${currentUser?.username ?: "unknown"} does not have required role: $role"
            )
        }
    }

    /**
     * Require the current user to be an admin
     */
    fun requireAdmin() {
        requireRole("ADMIN")
    }
}

class UnauthenticatedException(message: String) : RuntimeException(message)
class InsufficientPermissionsException(message: String) : RuntimeException(message)
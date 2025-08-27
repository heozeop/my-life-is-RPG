package com.mylifeisrpg.myliftisrpg.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class ApiKeyAuthenticationToken : AbstractAuthenticationToken {
    private val apiKey: String
    private val userPrincipal: Any?

    constructor(apiKey: String) : super(null) {
        this.apiKey = apiKey
        this.userPrincipal = null
        isAuthenticated = false
    }

    constructor(apiKey: String, userPrincipal: Any, authorities: Collection<GrantedAuthority>) : super(authorities) {
        this.apiKey = apiKey
        this.userPrincipal = userPrincipal
        isAuthenticated = true
    }

    override fun getCredentials(): String = apiKey

    override fun getPrincipal(): Any? = userPrincipal
}
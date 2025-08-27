package com.mylifeisrpg.myliftisrpg.config

import com.mylifeisrpg.myliftisrpg.security.ApiKeyAuthenticationFilter
import com.mylifeisrpg.myliftisrpg.security.SimpleApiKeyAuthenticationProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val apiKeyAuthenticationProvider: SimpleApiKeyAuthenticationProvider
) {

    @Bean
    fun authenticationManager(): AuthenticationManager {
        return ProviderManager(listOf(apiKeyAuthenticationProvider))
    }

    @Bean
    fun apiKeyAuthenticationFilter(authenticationManager: AuthenticationManager): ApiKeyAuthenticationFilter {
        return ApiKeyAuthenticationFilter(authenticationManager)
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        apiKeyAuthenticationFilter: ApiKeyAuthenticationFilter
    ): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/", "/health", "/actuator/**").permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .csrf { it.disable() }
            .cors { it.disable() }
        
        return http.build()
    }
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
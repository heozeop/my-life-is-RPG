package com.mylifeisrpg.myliftisrpg.config

import com.mylifeisrpg.myliftisrpg.security.ApiKeyAuthenticationFilter
import com.mylifeisrpg.myliftisrpg.security.DatabaseApiKeyAuthenticationProvider
import com.mylifeisrpg.myliftisrpg.security.AuthenticationLoggingFilter
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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val databaseApiKeyAuthenticationProvider: DatabaseApiKeyAuthenticationProvider,
    private val authenticationLoggingFilter: AuthenticationLoggingFilter
) {

    @Bean
    fun authenticationManager(): AuthenticationManager {
        return ProviderManager(listOf(databaseApiKeyAuthenticationProvider))
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
                    .requestMatchers("/auth/**").permitAll() // Allow public access to auth endpoints
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(authenticationLoggingFilter, UsernamePasswordAuthenticationFilter::class.java)
            .headers { headers ->
                headers
                    .frameOptions { frameOptions -> frameOptions.deny() }
                    .contentTypeOptions { }
                    .referrerPolicy { referrerPolicy ->
                        referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    }
                    .httpStrictTransportSecurity { hstsConfig ->
                        hstsConfig
                            .maxAgeInSeconds(31536000)
                            .includeSubDomains(true)
                            .preload(true)
                    }
            }
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
package com.mylifeisrpg.myliftisrpg.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mylifeisrpg.myliftisrpg.dto.LoginRequest
import com.mylifeisrpg.myliftisrpg.dto.RegisterRequest
import com.mylifeisrpg.myliftisrpg.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import kotlin.random.Random

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @Test
    @Order(1)
    fun `should register new user successfully`() {
        val registerRequest = RegisterRequest(
            username = "testuser${Random.nextInt(1000, 9999)}",
            password = "SecurePass123!"
        )

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.apiKey").exists())
            .andExpect(jsonPath("$.apiKey").value(org.hamcrest.Matchers.startsWith("ak_")))
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.userId").isNumber)
            .andExpect(jsonPath("$.username").value(registerRequest.username))
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    @Order(2)
    fun `should reject duplicate username registration`() {
        val username = "duplicateuser${Random.nextInt(1000, 9999)}"
        val registerRequest = RegisterRequest(
            username = username,
            password = "SecurePass123!"
        )

        // Register user first time
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)

        // Try to register same user again
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("Username '$username' is already taken"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    @Order(3)
    fun `should reject weak passwords`() {
        val registerRequest = RegisterRequest(
            username = "weakpassuser${Random.nextInt(1000, 9999)}",
            password = "weak"
        )

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.password").exists())
    }

    @Test
    @Order(4)
    fun `should validate password strength requirements`() {
        val testCases = listOf(
            "nouppercase123!" to "no uppercase",
            "NOLOWERCASE123!" to "no lowercase", 
            "NoNumbers!" to "no numbers",
            "NoSpecialChar123" to "no special characters",
            "Short1!" to "too short"
        )

        testCases.forEach { (password, description) ->
            val registerRequest = RegisterRequest(
                username = "testuser${Random.nextInt(1000, 9999)}",
                password = password
            )

            mockMvc.perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
        }
    }

    @Test
    @Order(5)
    fun `should login successfully with valid credentials`() {
        val username = "loginuser${Random.nextInt(1000, 9999)}"
        val password = "SecurePass123!"
        
        // Register user first
        val registerRequest = RegisterRequest(username = username, password = password)
        val registrationResult = mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val registrationResponse = objectMapper.readTree(registrationResult.response.contentAsString)
        val expectedApiKey = registrationResponse.get("apiKey").asText()

        // Login with same credentials
        val loginRequest = LoginRequest(username = username, password = password)
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.apiKey").value(expectedApiKey))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    @Order(6)
    fun `should reject invalid credentials`() {
        val username = "invalidcreduser${Random.nextInt(1000, 9999)}"
        val password = "SecurePass123!"
        
        // Register user first
        val registerRequest = RegisterRequest(username = username, password = password)
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)

        // Try to login with wrong password
        val loginRequest = LoginRequest(username = username, password = "WrongPassword123!")
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Invalid username or password"))
    }

    @Test
    @Order(7)
    fun `should reject login with non-existent user`() {
        val loginRequest = LoginRequest(
            username = "nonexistentuser${Random.nextInt(1000, 9999)}",
            password = "SecurePass123!"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Invalid username or password"))
    }

    @Test
    @Order(8)
    fun `should check username availability correctly`() {
        val existingUsername = "existinguser${Random.nextInt(1000, 9999)}"
        val availableUsername = "availableuser${Random.nextInt(1000, 9999)}"
        
        // Register a user first
        val registerRequest = RegisterRequest(
            username = existingUsername,
            password = "SecurePass123!"
        )
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)

        // Check existing username
        mockMvc.perform(
            get("/auth/check-username")
                .param("username", existingUsername)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(existingUsername))
            .andExpect(jsonPath("$.available").value(false))
            .andExpect(jsonPath("$.message").value("Username is already taken"))

        // Check available username
        mockMvc.perform(
            get("/auth/check-username")
                .param("username", availableUsername)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(availableUsername))
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.message").value("Username is available"))
    }

    @Test
    @Order(9)
    fun `should generate unique API keys for different users`() {
        val apiKeys = mutableSetOf<String>()
        
        repeat(10) { i ->
            val registerRequest = RegisterRequest(
                username = "uniqueuser$i${Random.nextInt(1000, 9999)}",
                password = "SecurePass123!"
            )

            val result = mockMvc.perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val response = objectMapper.readTree(result.response.contentAsString)
            val apiKey = response.get("apiKey").asText()
            
            assert(apiKey.startsWith("ak_")) { "API key should start with 'ak_'" }
            assert(apiKey.length > 10) { "API key should be reasonably long" }
            assert(!apiKeys.contains(apiKey)) { "API key should be unique" }
            
            apiKeys.add(apiKey)
        }
    }

    @Test
    @Order(10)
    @Transactional
    fun `should handle concurrent registration attempts gracefully`() {
        val username = "concurrentuser${Random.nextInt(1000, 9999)}"
        val password = "SecurePass123!"
        val numberOfThreads = 10
        val latch = CountDownLatch(numberOfThreads)
        
        val futures = (1..numberOfThreads).map { 
            CompletableFuture.supplyAsync {
                try {
                    latch.countDown()
                    latch.await() // Wait for all threads to be ready
                    
                    val registerRequest = RegisterRequest(username = username, password = password)
                    val result = mockMvc.perform(
                        post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest))
                    ).andReturn()
                    
                    result.response.status
                } catch (e: Exception) {
                    500 // Return error status if exception occurs
                }
            }
        }

        val results = futures.map { it.get() }
        
        // Exactly one should succeed (201), others should fail (409)
        val successCount = results.count { it == 201 }
        val conflictCount = results.count { it == 409 }
        
        assert(successCount == 1) { "Exactly one registration should succeed, but got $successCount" }
        assert(conflictCount == numberOfThreads - 1) { "Expected ${numberOfThreads - 1} conflicts, but got $conflictCount" }
    }

    @Test
    @Order(11)
    fun `should validate request content type`() {
        val registerRequest = RegisterRequest(
            username = "contenttypeuser${Random.nextInt(1000, 9999)}",
            password = "SecurePass123!"
        )

        // Test with wrong content type
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isUnsupportedMediaType)
    }

    @Test
    @Order(12)
    fun `should validate malformed JSON`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @Order(13)
    fun `should validate required fields`() {
        // Test missing username
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"password": "SecurePass123!"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.username").exists())

        // Test missing password
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username": "testuser"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.password").exists())
    }
}
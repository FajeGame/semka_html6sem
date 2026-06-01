package ru.semka

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/** Запускается в CI: USE_TESTCONTAINERS=true */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@EnabledIfEnvironmentVariable(named = "USE_TESTCONTAINERS", matches = "true")
class PostgresTestcontainersTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("semka")
            .withUsername("semka")
            .withPassword("semka")

        @JvmStatic
        @DynamicPropertySource
        fun datasource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun loginOnPostgres() {
        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"papa@test.ru","password":"password"}"""
        }.andExpect {
            status { isOk() }
        }
    }

    /** новый пустой кошелёк не должен пропадать из GET /wallets после создания */
    @Test
    fun createWalletPersistsInList() {
        val token = loginToken("papa@test.ru", "password")
        mockMvc.post("/wallets") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
            content = """{"name":"Тест список"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Тест список") }
        }
        mockMvc.get("/wallets") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$[?(@.name == 'Тест список')]") { isNotEmpty() }
        }
    }

    private fun loginToken(email: String, password: String): String {
        val loginResult = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$email","password":"$password"}"""
        }.andExpect {
            status { isOk() }
        }.andReturn()
        return Regex(""""token"\s*:\s*"([^"]+)"""").find(loginResult.response.contentAsString)!!.groupValues[1]
    }
}

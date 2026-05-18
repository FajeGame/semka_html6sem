package ru.semka

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun demoLoginAndMe() {
        val loginJson = """{"email":"papa@test.ru","password":"password"}"""
        val loginResult = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = loginJson
        }.andExpect {
            status { isOk() }
        }.andReturn()

        val token = Regex(""""token"\s*:\s*"([^"]+)"""").find(loginResult.response.contentAsString)!!.groupValues[1]

        mockMvc.get("/auth/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.nick") { value("papa") }
        }
    }
}

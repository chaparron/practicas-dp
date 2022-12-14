package adapters.rest.handler

import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.model.Role
import domain.model.User
import domain.services.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod

@ExtendWith(MockitoExtension::class)
internal class GetUserHandlerTest {

    private var service: UserService = mock()
    private var context: Context = mock()

    private lateinit var sut: GetUserHandler

    companion object {
        private const val PATH = "/dp/user"
        private const val ACCESS_TOKEN = "my-token"
        private const val USER_ID_PARAM = "userId"
        private const val USER_ID_VALUE = "77"
    }

    @BeforeEach
    fun setUp() {
        sut = GetUserHandler(service)
    }

    private val anyUser = User(
        name = "Tete",
        userId = USER_ID_VALUE.toLong(),
        mail = "tete@wabi.com",
        country = "Spain",
        active = true,
        phone = "+3465432112",
        role = Role.USER,
        createdAt = "2018-10-10",
        lastLogin = "30-10-2022",
        orders = listOf("123", "456", "789")
    )

    @Test
    fun `should return user giving an userId`() {
        // Given
        val userId = anyUser.userId
        val event = anyApiGatewayProxyRequestEvent()

        whenever(service.get(userId)).thenReturn(anyUser)
        // When
        val response = sut.handleRequest(event, context).body
        // Then
        val expected = anyUser
        assertEquals(expected.toString(), response)

        verify(service).get(userId)

    }

    private fun anyApiGatewayProxyRequestEvent(): APIGatewayProxyRequestEvent =
        apiGatewayEventRequest(
            method = HttpMethod.GET,
            path = PATH,
            authorization = ACCESS_TOKEN,
            queryParams = mapOf(USER_ID_PARAM to USER_ID_VALUE)
        )

}

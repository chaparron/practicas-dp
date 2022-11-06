package adapters.rest.handler

import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.services.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.springframework.http.HttpMethod

@ExtendWith(MockitoExtension::class)
internal class DeleteUserHandlerTest {

    private var service: UserService = mock()
    private var context: Context = mock()

    private lateinit var sut: DeleteUserHandler

    companion object {
        private const val PATH = "/dp/user"
        private const val ACCESS_TOKEN = "my-token"
        private const val USER_ID_PARAM = "userId"
        private const val USER_ID_VALUE = "77"
    }

    @BeforeEach
    fun setup() {
        sut = DeleteUserHandler(service)
    }

    //<editor-fold desc="val commented anyUser">
//    private val anyUser = User(
//        name = "Tete",
//        userId = USER_ID_VALUE.toLong(),
//        mail = "tete@wabi.com",
//        country = "Spain",
//        active = true,
//        phone = "+3465432112",
//        role = Role.USER,
//        createdAt = "2018-10-10",
//        lastLogin = "30-10-2022",
//        orders = listOf("123", "456", "789")
//    )
    //</editor-fold>

    //<editor-fold desc="private fun anyApiGatewayProxyRequestEvent()">
    private fun anyApiGatewayProxyRequestEvent(): APIGatewayProxyRequestEvent =
        apiGatewayEventRequest(
            method = HttpMethod.DELETE,
            path = PATH,
            authorization = ACCESS_TOKEN,
            queryParams = mapOf(USER_ID_PARAM to USER_ID_VALUE)
        )
    //</editor-fold>

    @Test
    fun `Can handle request`() {

//        val userId = anyUser.userId
        val event = anyApiGatewayProxyRequestEvent()
        // Given
//        whenever(service.get(userId)).thenReturn("WTF")
        // When
        sut.handleRequest(event, context)
        // Then
        // Verify
    }
}

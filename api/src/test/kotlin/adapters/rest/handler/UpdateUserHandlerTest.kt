package adapters.rest.handler

import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import configuration.MainConfiguration
import domain.model.Role
import domain.model.User
import domain.services.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UpdateUserHandlerTest(
    @Mock private val context: Context,
    @Mock private val service: UserService
) {

    //<editor-fold desc="mocking old way commented">
    //    private var context: Context = mock()
//    private var service: UserService = mock()
//    private var jsonMapper: Json = Json {
//        encodeDefaults = true
//        ignoreUnknownKeys = true
//    }
//    private lateinit var sut: UpdateUserHandler
//
//    @BeforeEach
//    fun setup() {
//        Mockito.reset(service)
//        sut = UpdateUserHandler(service, jsonMapper)
//    }
    //</editor-fold>

    private val jsonMapper = MainConfiguration.jsonMapper
    private val sut = UpdateUserHandler(service, jsonMapper)

    companion object {
        const val USER_PATH = UpdateUserHandler.USER_PATH
        const val USER_ID_VALUE = "77"
        private const val ACCESS_TOKEN = "my-token"
    }

    //<editor-fold desc="private val updatedUser">
    private val updatedUser = User(
        name = "Tete",
        userId = USER_ID_VALUE.toLong(),
        mail = "tacktickVictor@wabi.com",
        country = "Argentina",
        active = true,
        phone = "+123456789",
        role = Role.ADMIN,
        createdAt = "2018-10-10",
        lastLogin = "06-11-2022",
        orders = listOf("123", "456", "789", "1010")
    )
    //</editor-fold>

    private fun anyUpdateUserRequest() = UpdateUserHandlerRequest(
        name = "Tete",
        userId = USER_ID_VALUE.toLong(),
        mail = "tacktickVictor@wabi.com",
        country = "Argentina",
        active = true,
        phone = "+123456789",
        role = Role.ADMIN,
        createdAt = "2018-10-10",
        lastLogin = "06-11-2022",
        orders = listOf("123", "456", "789", "1010")
    )

    //<editor-fold desc="private fun anyAPIGatewayProxyRequestEvent">
    private fun anyAPIGatewayProxyRequestEvent(
        request: UpdateUserHandlerRequest = anyUpdateUserRequest()
    ): APIGatewayProxyRequestEvent =
        apiGatewayEventRequest(
            method = HttpMethod.DELETE,
            path = USER_PATH,
            authorization = ACCESS_TOKEN,
            body = """
            {"name":"${request.name}","userId":${request.userId},"mail":"${request.mail}","country":"${request.country}","active":${request.active},"phone":"${request.phone}","role":"${request.role}","createdAt":"${request.createdAt}","lastLogin":"${request.lastLogin}","orders":["123", "456", "789", "1010"]} 
        """.trimIndent()
        )
    //</editor-fold>

    @Test
    fun `Should retrieve updated user on update user`() {
        // Given
        val event = anyAPIGatewayProxyRequestEvent()
        // When
        whenever(service.update(any())).thenReturn(updatedUser)
        val response = sut.handleRequest(event, context).body
        // Then
        val expected = """
            {"name":"${updatedUser.name}","userId":${updatedUser.userId},"mail":"${updatedUser.mail}","country":"${updatedUser.country}","active":${updatedUser.active},"phone":"${updatedUser.phone}","role":"${updatedUser.role}","createdAt":"${updatedUser.createdAt}","lastLogin":"${updatedUser.lastLogin}","orders":["123","456","789","1010"]}
        """.trimIndent()
        assertEquals(expected, response)
        // Verify
        verify(service).update(any())
    }

}

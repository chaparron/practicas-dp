package adapters.rest.handler

import CreateUserHandlerRequest
import SaveUserHandler
import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.model.Role
import domain.model.User
import domain.services.UserService
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpMethod

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SaveUserHandlerTest {

    private var context: Context = mock()
    private var service: UserService = mock()
    private var jsonMapper: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private lateinit var sut: SaveUserHandler

    companion object{
        private const val PATH = "/dp/user"
        private const val ACCESS_TOKEN = "my-token"
    }

    @BeforeEach
    fun setUp() {
        Mockito.reset(service)
        sut = SaveUserHandler(service, jsonMapper)
    }

    @Test
    fun `given state validation ok when saveUser then return valid response `() {
        val request = anySaveUserRequest()
        val EXPECTED_JSON_RESPONSE = """
            {"name":"${request.name}","userId":${request.userId},"mail":"${request.mail}","country":"${request.country}","active":${request.active},"phone":"${request.phone}","role":"${request.role}","createdAt":"${request.createdAt}","lastLogin":"${request.lastLogin}","orders":["123","456","789"]}
        """.trimIndent()

        whenever(service.save(any())).thenReturn(userResponse)

        val response = sut.handleRequest(anyApiGatewayProxyRequestEvent(request), context).body

        println("\u001b[7m $EXPECTED_JSON_RESPONSE \u001b[0m}")
        println("\u001b[7m $response \u001b[0m}")
        assertEquals(EXPECTED_JSON_RESPONSE,response)
    }

    private fun anyApiGatewayProxyRequestEvent(
        request: CreateUserHandlerRequest = anySaveUserRequest()
    ): APIGatewayProxyRequestEvent = apiGatewayEventRequest(
        method = HttpMethod.POST,
        path = PATH,
        authorization = ACCESS_TOKEN,
        body = """
            {"name":"${request.name}","userId":${request.userId},"mail":"${request.mail}","country":"${request.country}","active":${request.active},"phone":"${request.phone}","role":"${request.role}","createdAt":"${request.createdAt}","lastLogin":"${request.lastLogin}","orders":["123", "456", "789"]} 
        """.trimIndent() //,"orders":["123", "456", "789"]}
    )

    private fun anySaveUserRequest() = CreateUserHandlerRequest(
        name = "Tete",
        userId = 123L,
        mail = "tete@wabi.com",
        country = "Argentina",
        active = true,
        phone = "12344321",
        role = Role.ADMIN,
        createdAt = "yesterday",
        lastLogin = "Today",
        orders = listOf("123", "456", "789")
    )

    private val userResponse = User(
        name = "Tete",
        userId = 123L,
        mail = "tete@wabi.com",
        country = "Argentina",
        active = true,
        phone = "12344321",
        role = Role.ADMIN,
        createdAt = "yesterday",
        lastLogin = "Today",
        orders = listOf("123", "456", "789")
    )
}


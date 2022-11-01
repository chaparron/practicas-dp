package digitalpayments.sdk

import ACCESS_TOKEN
import CLIENT_TOKEN_PREFIX
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import digitalpayments.sdk.configuration.SdkConfiguration
import digitalpayments.sdk.model.Provider
import domain.model.RoleResponse
import domain.model.UserResponse
import kotlinx.serialization.encodeToString
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import reactor.test.StepVerifier
import java.net.URI

internal class HttpDigitalPaymentsSdkGetUserTest : AbstractSdkTest() {

    companion object {
        private const val PATH = "/dp/user"
        private const val USER_ID_KEY = "userId"
        private const val USER_ID = "77"
    }

    private val root = URI.create("http://localhost:${port()}")
    private val digitalPaymentsSdk: DigitalPaymentsSdk = HttpDigitalPaymentsSdk(root)
    private val mapper = SdkConfiguration.jsonMapper

    private val anyUser = UserResponse(
        name = "Tete",
        userId = USER_ID.toLong(),
        mail = "tete@wabi.com",
        country = "Spain",
        active = true,
        phone = "+3465432112",
        role = RoleResponse.USER,
        createdAt = "2018-10-10",
        lastLogin = "30-10-2022",
        orders = listOf("123", "456", "789")
    )

    @Test
    fun `given valid request when getUser then return user`() {

        val response = anyUser

        WireMock.stubFor(
            WireMock.any(WireMock.urlPathEqualTo(PATH))
                .withQueryParam(
                    USER_ID_KEY,
                    WireMock.equalTo(USER_ID)
                )
                .withHeader(HttpHeaders.CONTENT_TYPE, EqualToPattern(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.AUTHORIZATION, EqualToPattern("$CLIENT_TOKEN_PREFIX $ACCESS_TOKEN"))
                .willReturn(WireMock.ok(mapper.encodeToString(response)))
        )

        StepVerifier
            .create(digitalPaymentsSdk.getUser(USER_ID, ACCESS_TOKEN))
            .expectNext(response)
            .verifyComplete()

    }
}

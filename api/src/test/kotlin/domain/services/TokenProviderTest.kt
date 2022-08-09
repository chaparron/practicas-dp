package domain.services

import domain.services.Wabi2bTokenProvider.Companion.SAFE_EXPIRATION_THRESHOLD
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import randomString
import reactor.core.publisher.Mono.just
import wabi2b.sdk.api.Wabi2bSdk
import wabi2b.sdk.api.dto.UserLogin
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class TokenProviderTest {
    private lateinit var sut: TokenProvider

    @Mock
    private lateinit var wabi2bSdk: Wabi2bSdk
    private val clientId = randomString()
    private val clientSecret = randomString()

    @Mock
    private lateinit var clock: Clock

    @BeforeEach
    fun setup() {
        sut = Wabi2bTokenProvider(
            wabi2bSdk = wabi2bSdk,
            dpClientUser = clientId,
            dpClientSecret = clientSecret,
            clock = clock
        )
    }

    @Test
    fun `requesting a clientToken successfully`() {
        val now = Instant.now()
        whenever(clock.instant()).doReturn(now)
        val anyUserLogin = anyUserLogin()
        whenever(wabi2bSdk.clientLogin(clientId, clientSecret)).doReturn(just(anyUserLogin))

        assertEquals(anyUserLogin.accessToken, sut.getClientToken())
    }

    @Test
    fun `requesting multiple times a token when the first is not expired should return the cached token`() {
        val now = Instant.now()
        val validForNext2DaysToken = anyUserLogin(expiration = now.plus(2L, ChronoUnit.DAYS).toEpochMilli())
        whenever(clock.instant()).doReturn(now)
        whenever(wabi2bSdk.clientLogin(any(), any())).doReturn(just(validForNext2DaysToken))

        repeat(2) {
            assertEquals(validForNext2DaysToken.accessToken, sut.getClientToken())
        }
        verify(wabi2bSdk).clientLogin(clientId, clientSecret)
    }

    @Test
    fun `requesting multiple times a token when the first is expired should return a new token`() {
        val now = Instant.now()
        val alreadyExpired = -(SAFE_EXPIRATION_THRESHOLD + 1)
        val expiredToken = anyUserLogin(expiration = alreadyExpired)
        val newToken = anyUserLogin()
        whenever(clock.instant()).doReturn(now)
        whenever(wabi2bSdk.clientLogin(any(), any())).doReturn(just(expiredToken), just(newToken))

        assertEquals(expiredToken.accessToken, sut.getClientToken())
        assertEquals(newToken.accessToken, sut.getClientToken())
        verify(wabi2bSdk, times(2)).clientLogin(clientId, clientSecret)
    }

    @Test
    fun `requesting a token while the cached one is not expired and withing safe expiration threshold should return cached token`() {
        val now = Instant.now()
        val withingThreshold = -(SAFE_EXPIRATION_THRESHOLD)
        val withingThresholdToken = anyUserLogin(expiration = withingThreshold)
        whenever(clock.instant()).doReturn(now)
        whenever(wabi2bSdk.clientLogin(any(), any())).doReturn(just(withingThresholdToken))

        repeat(2) {
            assertEquals(withingThresholdToken.accessToken, sut.getClientToken())
        }
        verify(wabi2bSdk).clientLogin(clientId, clientSecret)
    }

    @Test
    fun `requesting a token while the cached one is not expired and not within safe expiration threshold should return new token`() {
        val now = Instant.now()
        val expiredComparedToSafeExpirationThreshold = -SAFE_EXPIRATION_THRESHOLD - 1
        val validTokenButExpiredAgainstThreshold = anyUserLogin(expiration = expiredComparedToSafeExpirationThreshold)
        val newToken = anyUserLogin()
        whenever(clock.instant()).doReturn(now)
        whenever(wabi2bSdk.clientLogin(any(), any())).doReturn(
            just(validTokenButExpiredAgainstThreshold),
            just(newToken)
        )

        assertEquals(validTokenButExpiredAgainstThreshold.accessToken, sut.getClientToken())
        assertEquals(newToken.accessToken, sut.getClientToken())

        verify(wabi2bSdk, times(2)).clientLogin(clientId, clientSecret)
    }

    private fun anyUserLogin(expiration: Long = Instant.now().toEpochMilli()): UserLogin = UserLogin(
        accessToken = randomString(),
        tokenType = randomString(),
        expiresIn = expiration,
        scope = randomString()
    )
}


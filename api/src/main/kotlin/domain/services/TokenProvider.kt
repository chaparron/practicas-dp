package domain.services

import domain.model.errors.DpException
import org.slf4j.LoggerFactory
import wabi2b.sdk.api.Wabi2bSdk
import wabi2b.sdk.api.dto.UserLogin
import java.time.Clock
import java.time.Instant

interface TokenProvider {
    fun getClientToken(): String
}

class Wabi2bTokenProvider(
    private val wabi2bSdk: Wabi2bSdk,
    private val dpClientUser: String,
    private val dpClientSecret: String,
    private val clock: Clock
) : TokenProvider {
    private lateinit var clientAccessToken: CachedUserLogin

    companion object {
        private val logger = LoggerFactory.getLogger(Wabi2bTokenProvider::class.java)

        /**
         * Wabi2b use to make token expired earlier to avoid edge cases
         */
        const val SAFE_EXPIRATION_THRESHOLD = 60L
    }

    override fun getClientToken(): String {
        if (this::clientAccessToken.isInitialized) {
            if (clientAccessToken.isExpired())
                clientAccessToken = requestTokenToSdk().also {
                    logger.trace("cached token has expired(${clientAccessToken.expiresOn}) and has been renewed")
                }.let {
                    mapToCachedUserLogin(it)
                }
        } else
            clientAccessToken = mapToCachedUserLogin(requestTokenToSdk())

        return clientAccessToken.accessToken
    }

    private fun mapToCachedUserLogin(it: UserLogin): CachedUserLogin {
        val expiresOn = clock.instant()
            .plusSeconds(it.expiresIn)
        return CachedUserLogin(it.accessToken, expiresOn)
    }

    private fun CachedUserLogin.isExpired() =
        expiresOn.isBefore(clock.instant().minusSeconds(SAFE_EXPIRATION_THRESHOLD))

    private fun requestTokenToSdk() = wabi2bSdk.clientLogin(dpClientUser, dpClientSecret).block()
        ?: throw DpException.unknown( "Could not get a digital payments client token")

    data class CachedUserLogin(
        val accessToken: String,
        val expiresOn: Instant
    )
}

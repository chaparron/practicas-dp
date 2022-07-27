package digitalpayments.sdk

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner.StrictStubs

@RunWith(StrictStubs::class)
abstract class AbstractSdkTest {

    companion object {
        private val wireMockConfiguration = WireMockConfiguration.wireMockConfig().port(11111)
        private val port: Int = wireMockConfiguration.portNumber()
    }

    @get:Rule
    var wireMockRule = WireMockRule(wireMockConfiguration)

    fun port(): String {
        return port.toString()
    }

}

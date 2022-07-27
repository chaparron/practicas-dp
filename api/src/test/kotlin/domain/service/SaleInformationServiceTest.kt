package domain.service

import com.wabi2b.jpmc.sdk.usecase.sale.SaleInformation
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import configuration.EnvironmentVariable
import domain.services.SaleInformationService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SaleInformationServiceTest {

    @Mock
    private lateinit var saleServiceSdk: SaleService

    @Mock
    private lateinit var configuration: EnvironmentVariable.JpmcConfiguration

    @InjectMocks
    private lateinit var sut: SaleInformationService

    @Test
    fun `given a valid request when getSaleInformation then return valid information`() {
        val saleInformation = anySaleInformation()
        whenever(saleServiceSdk.getSaleInformation(any())).thenReturn(saleInformation)
        wheneverForConfigurations()

        val response = sut.getSaleInformation("2995")

        assertAll(
            "Check values",
            { assertEquals(saleInformation.bankId, response.bankId) },
            { assertEquals(saleInformation.merchantId, response.merchantId) },
            { assertEquals(saleInformation.terminalId, response.terminalId) },
            { assertEquals(saleInformation.encData, response.encData) },
        )

        verify(saleServiceSdk).getSaleInformation(any())
        verifyForConfigurations()
    }


    private fun anySaleInformation() = SaleInformation(
        bankId = "001002",
        merchantId = "100000000010588",
        terminalId = "10010186",
        encData = "0123456789abcdefgh"
    )

    private fun wheneverForConfigurations() {
        whenever(configuration.version).thenReturn("")
        whenever(configuration.passCode).thenReturn("")
        whenever(configuration.bankId).thenReturn("")
        whenever(configuration.terminalId).thenReturn("")
        whenever(configuration.merchantId).thenReturn("")
        whenever(configuration.mcc).thenReturn("")
        whenever(configuration.currency).thenReturn("")
        whenever(configuration.returnUrl).thenReturn("")
    }

    private fun verifyForConfigurations() {
        verify(configuration).bankId
        verify(configuration).terminalId
        verify(configuration).version
        verify(configuration).passCode
        verify(configuration).merchantId
        verify(configuration).mcc
        verify(configuration).currency
        verify(configuration).returnUrl
    }
}

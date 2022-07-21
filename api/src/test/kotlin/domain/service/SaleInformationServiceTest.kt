package domain.service

import com.wabi2b.jpmc.sdk.usecase.sale.SaleInformation
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import domain.services.SaleInformationService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

//@ExtendWith(MockitoExtension::class)
class SaleInformationServiceTest {

    @MockK
    private lateinit var saleServiceSdk: SaleService

    @InjectMockKs
    private lateinit var sut: SaleInformationService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `given a valid request when getSaleInformation then return valid information`() {
        val saleInformation = SaleInformation(
            bankId = "001002",
            merchantId = "100000000010588",
            terminalId = "10010186",
            encData = "0123456789abcdefgh"
        )
        every { saleServiceSdk.getSaleInformation(any()) }.returns(saleInformation)

        val response = sut.getSaleInformation("2995")

        assertAll(
            "Check values",
            { assertEquals(saleInformation.bankId, response.bankId) },
            { assertEquals(saleInformation.merchantId, response.merchantId) },
            { assertEquals(saleInformation.terminalId, response.terminalId) },
            { assertEquals(saleInformation.encData, response.encData) },
        )
    }

}

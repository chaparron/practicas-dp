package domain.services.providers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import randomLong
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class PaymentProviderServiceTest {

    companion object {
        private val defaultProvider = Provider.JP_MORGAN
    }

    @Mock
    private lateinit var jpmProviderService: ProviderService

    @InjectMocks
    private lateinit var sut: PaymentProviderService

    @Test
    fun `given a supplier with valid state returns a provider list including default provider`() {
        assertFor(true, ::defaultProviders)
    }

    @Test
    fun `given a supplier with invalid state returns an empty provider list`() {
        assertFor(false, ::emptyList)
    }

    private fun assertFor(serviceResponse: Boolean, expected: () -> List<Provider>) {
        //Given
        val supplierId = randomLong()

        //When
        whenever(jpmProviderService.isAccepted(any()))
            .thenReturn(serviceResponse)

        //Then
        assertEquals(expected(), sut.availableProviders(supplierId))

        //Verify
        verify(jpmProviderService).isAccepted(supplierId)
    }

    private fun defaultProviders() = listOf(defaultProvider)

}

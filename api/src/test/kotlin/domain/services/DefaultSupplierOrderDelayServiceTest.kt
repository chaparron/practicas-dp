package domain.services

import adapters.repositories.supplierorderdelay.SupplierOrderDelayRepository
import domain.model.SupplierOrderDelay
import domain.model.SupplierOrderDelayEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class DefaultSupplierOrderDelayServiceTest {

    private var repository: SupplierOrderDelayRepository = mock()
    private lateinit var sut: DefaultSupplierOrderDelayService

    @BeforeEach
    fun setUp() {
        sut = DefaultSupplierOrderDelayService(repository)
    }

    private val supplierOrderDelayedEvent = SupplierOrderDelayEvent(
        supplierOrderId = 77L,
        delay = true,
        delayTime = 60
    )

    @Test
    fun `should retrieve the same result saved with the same order delay supplier id`() {
        // Given
        val supplierOrderDelayId = supplierOrderDelayedEvent.supplierOrderId
        val supplierOrderDelay = supplierOrderDelayedEvent.toSupplierOrderDelay()

        whenever(repository.save(supplierOrderDelay)).thenReturn(supplierOrderDelay)
        whenever(repository.get(supplierOrderDelayId)).thenReturn(supplierOrderDelay)
        // When
        val expected = sut.save(supplierOrderDelayedEvent)
        val actual = sut.get(supplierOrderDelayId)
        // Then
        assertEquals(expected, actual)

        verify(repository).save(supplierOrderDelay)
        verify(repository).get(supplierOrderDelayId)
    }

    fun SupplierOrderDelayEvent.toSupplierOrderDelay(): SupplierOrderDelay {
        return SupplierOrderDelay(
            supplierOrderId = this.supplierOrderId,
            delay = this.delay,
            delayTime = this.delayTime
        )
    }
}

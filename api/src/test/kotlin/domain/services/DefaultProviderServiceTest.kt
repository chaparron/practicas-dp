package domain.services

import anySupplier
import domain.services.providers.jpmc.DefaultProviderService
import kotlin.test.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import randomLong

class DefaultProviderServiceTest {

    private val supplierService: SupplierService = mock()

    private val sut = DefaultProviderService(supplierService)

    @Test
    fun `Should return true if supplier has bank account`() {
        val someSupplierId = randomLong()
        val someSupplier = anySupplier(someSupplierId)

        whenever(supplierService.get(someSupplierId)).thenReturn(someSupplier)

        assert(sut.isAccepted(someSupplierId))
    }

    @Test
    fun `Should return false if supplier has not bank account`() {
        val someSupplierId = randomLong()
        val someSupplier = anySupplier(someSupplierId, "")

        whenever(supplierService.get(someSupplierId)).thenReturn(someSupplier)

        assertFalse(sut.isAccepted(someSupplierId))
    }

    @Test
    fun `Should return false if supplierService fail`() {
        val someSupplierId = randomLong()

        whenever(supplierService.get(someSupplierId)).thenThrow(RuntimeException())

        assertFalse(sut.isAccepted(someSupplierId))
    }

}

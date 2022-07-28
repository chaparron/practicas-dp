package domain.service

import adapters.repositories.SupplierRepository
import anySupplier
import domain.services.DefaultSupplierService
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DefaultSupplierServiceTest {

    private val supplierRepository: SupplierRepository = mock()

    private val sut = DefaultSupplierService(supplierRepository)

    @Test
    fun `can save a supplier`() {
        val supplier = anySupplier()
        doReturn(supplier).whenever(supplierRepository).save(supplier)

        val actual = sut.save(supplier)

        assertEquals(supplier, actual)
    }

    @Test
    fun `can retrieve saved supplier`(){
        val supplier = anySupplier()
        doReturn(supplier).whenever(supplierRepository).save(supplier)
        doReturn(supplier).whenever(supplierRepository).get(supplier.supplierId)

        sut.save(supplier)

        val actual = sut.get(supplier.supplierId)

        assertEquals(supplier, actual)
    }
}

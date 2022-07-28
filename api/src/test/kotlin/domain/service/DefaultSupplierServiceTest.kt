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
    fun `can save a bank account`() {
        val bankAccount = anySupplier()
        doReturn(bankAccount).whenever(supplierRepository).save(bankAccount)

        val actual = sut.save(bankAccount)

        assertEquals(bankAccount, actual)
    }

    @Test
    fun `can retrieve saved bank account`(){
        val bankAccount = anySupplier()
        doReturn(bankAccount).whenever(supplierRepository).save(bankAccount)
        doReturn(bankAccount).whenever(supplierRepository).get(bankAccount.supplierId)

        sut.save(bankAccount)

        val actual = sut.get(bankAccount.supplierId)

        assertEquals(bankAccount, actual)
    }
}

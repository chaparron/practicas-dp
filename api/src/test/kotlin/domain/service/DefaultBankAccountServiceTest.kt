package domain.service

import adapters.repositories.BankAccountRepository
import anyBankAccount
import domain.services.DefaultBankAccountService
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DefaultBankAccountServiceTest {

    private val bankAccountRepository: BankAccountRepository = mock()

    private val sut = DefaultBankAccountService(bankAccountRepository)

    @Test
    fun `can save a bank account`() {
        val bankAccount = anyBankAccount()
        doReturn(bankAccount).whenever(bankAccountRepository).save(bankAccount)

        val actual = sut.save(bankAccount)

        assertEquals(bankAccount, actual)
    }

    @Test
    fun `can retrieve saved bank account`(){
        val bankAccount = anyBankAccount()
        doReturn(bankAccount).whenever(bankAccountRepository).save(bankAccount)
        doReturn(bankAccount).whenever(bankAccountRepository).get(bankAccount.supplierId)

        sut.save(bankAccount)

        val actual = sut.get(bankAccount.supplierId)

        assertEquals(bankAccount, actual)
    }
}

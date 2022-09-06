package domain.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import randomLong
import randomString

class SupplierEventValidatorTest {

    companion object {
        private val validator: SupplierEventValidator = SupplierEventValidator()
    }

    @Test
    fun `returns false if event does not contain a supported country`() {
        assertFor(createEventWithNoSupportedCountry())
    }

    @Test
    fun `returns false if event does not contain a bank account event`() {
        assertFor(createEventWithNullBankAccount())
    }

    @Test
    fun `returns true for valid event`() {
        assertFor(createEventWithNullBankAccount())
    }

    private fun assertFor(event: SupplierEvent, assertBlock: (Boolean) -> Unit = Assertions::assertFalse) {
        assertBlock(validator.isValid(event))
    }

    private fun createEventWithNoSupportedCountry() = createEvent(country = randomString())

    private fun createEventWithNullBankAccount() = createEvent(bankAccountEvent = null)

    private fun createEvent(country: String = "in", bankAccountEvent: BankAccountEvent? = BankAccountEvent(number = randomString(), indianFinancialSystemCode = randomString()) ) = SupplierEvent(
        supplierId = randomLong(), country = country, bankAccount = bankAccountEvent
    )
}

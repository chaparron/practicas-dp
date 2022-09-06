package domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import randomLong
import randomString

@ExtendWith(MockitoExtension::class)
class SupplierEventTest {

    private var validator: SupplierEventValidator = mock()

    private var block: (Supplier) -> Unit = mock()

    @Test
    fun `executes block for valid event`() {
        assertFor(true) {
            verify(block)(it.toSupplier())
        }
    }

    @Test
    fun `ignores block for invalid event`() {
        assertFor(false) {
            verifyNoInteractions(block)
        }
    }

    private fun assertFor(mockResponse: Boolean, verifyBlock: (SupplierEvent) -> Unit) {
        //Given
        val event = createEvent()

        //When
        whenever(validator.isValid(event)).thenReturn(mockResponse)
        whenever(block.invoke(any())).thenReturn(Unit)

        event.doHandle(validator, block)

        //Verify
        verifyBlock(event)
    }

    private fun createEvent() = SupplierEvent(
        supplierId = randomLong(), country = randomString(), bankAccount = BankAccountEvent(number = randomString(), indianFinancialSystemCode = randomString())
    )

}

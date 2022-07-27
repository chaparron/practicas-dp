package domain.functions

import anyBankAccount
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import configuration.Configuration
import configuration.TestConfiguration
import domain.model.BankAccount
import domain.model.BankAccountEvent
import domain.model.SupplierEvent
import domain.services.BankAccountService
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.*
import randomString
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankAccountListenerFunctionTest {

    private lateinit var configuration: Configuration
    private lateinit var json: Json
    private lateinit var bankAccountService: BankAccountService
    private val context: Context = mock()
    private lateinit var sut: BankAccountListenerFunction

    @BeforeEach
    fun setUp() {
        configuration = TestConfiguration.mockedInstance()
        json = configuration.jsonMapper
        bankAccountService = configuration.bankAccountListenerFunction.bankAccountService
        sut = configuration.bankAccountListenerFunction
    }

    @Test
    fun `can handle valid event`() {
        val event = buildSnsEvent()

        sut.handleRequest(event, context)

        verifyServiceInvocation(event)
    }

    @Test
    fun `propagates exception when bank account service fails`() {
        val event = buildSnsEvent()
        whenever(bankAccountService.save(any())).thenThrow(RuntimeException())

        assertFailsWith<RuntimeException> {
            sut.handleRequest(event, context)
        }

        verifyServiceInvocation(event)
    }
    @Test
    fun `propagates the deserialization exception when sns message is not a supplier event`() {
        val event = json.encodeToString(randomString()).let {
            SNSEvent().withRecords(listOf(
                SNSEvent.SNSRecord().withSns(SNSEvent.SNS().withMessage((it)))
            ))
        }

        assertFailsWith<SerializationException> {
            sut.handleRequest(event, context)
        }

        verifyNoInteractions(bankAccountService)
    }

    private fun buildSnsEvent(): SNSEvent = json.encodeToString(anyBankAccount().toSupplierEvent()).let {
        SNSEvent().withRecords(listOf(
            SNSEvent.SNSRecord().withSns(SNSEvent.SNS().withMessage((it)))
        ))
    }

    private fun BankAccount.toSupplierEvent(): SupplierEvent {
        return SupplierEvent(
            supplierId = supplierId,
            bankAccount = BankAccountEvent(
                number = number,
                indianFinancialSystemCode = indianFinancialSystemCode
            )
        )
    }

    private fun verifyServiceInvocation(event: SNSEvent) {
        verify(bankAccountService).save(json.decodeFromString<SupplierEvent>(event.records.first().sns.message).toBankAccount())
    }

}

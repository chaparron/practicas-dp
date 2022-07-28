package domain.functions

import anySupplier
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import configuration.Configuration
import configuration.TestConfiguration
import domain.model.Supplier
import domain.model.BankAccountEvent
import domain.model.SupplierEvent
import domain.services.SupplierService
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
class SupplierListenerFunctionTest {

    private lateinit var configuration: Configuration
    private lateinit var json: Json
    private lateinit var supplierService: SupplierService
    private val context: Context = mock()
    private lateinit var sut: SupplierListenerFunction

    @BeforeEach
    fun setUp() {
        configuration = TestConfiguration.mockedInstance()
        json = configuration.jsonMapper
        supplierService = configuration.supplierListenerFunction.supplierService
        sut = configuration.supplierListenerFunction
    }

    @Test
    fun `can handle valid event`() {
        val event = buildSnsEvent()

        sut.handleRequest(event, context)

        verifyServiceInvocation(event)
    }

    @Test
    fun `propagates exception when supplier service fails`() {
        val event = buildSnsEvent()
        whenever(supplierService.save(any())).thenThrow(RuntimeException())

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

        verifyNoInteractions(supplierService)
    }

    private fun buildSnsEvent(): SNSEvent = json.encodeToString(anySupplier().toSupplierEvent()).let {
        SNSEvent().withRecords(listOf(
            SNSEvent.SNSRecord().withSns(SNSEvent.SNS().withMessage((it)))
        ))
    }

    private fun Supplier.toSupplierEvent(): SupplierEvent {
        return SupplierEvent(
            supplierId = supplierId,
            state = state,
            bankAccount = BankAccountEvent(
                number = bankAccountNumber,
                indianFinancialSystemCode = indianFinancialSystemCode
            )
        )
    }

    private fun verifyServiceInvocation(event: SNSEvent) {
        verify(supplierService).save(json.decodeFromString<SupplierEvent>(event.records.first().sns.message).toSupplier())
    }

}

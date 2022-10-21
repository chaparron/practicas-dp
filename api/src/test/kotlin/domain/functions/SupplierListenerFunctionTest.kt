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

    companion object {
        private const val country = "in"
    }

    private lateinit var configuration: Configuration
    private lateinit var json: Json
    private lateinit var supplierService: SupplierService
    private val context: Context = mock()
    private lateinit var sut: SupplierListenerFunction

    @BeforeEach
    fun setUp() {
        json = configuration.jsonMapper
        supplierService = configuration.supplierListenerFunction.supplierService
        sut = configuration.supplierListenerFunction
        configuration = TestConfiguration.mockedInstance()
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

    @Test
    fun `handle a supplier payload with bank account`() {
        //Given
        val payload = fullPayload("""{"number":"123","ifsc":"616"}""")
        val event = buildSnsEvent(payload)

        //When
        sut.handleRequest(event, context)

        //Verify
        verifyServiceInvocation(event)
    }

    @Test
    fun `ignores a supplier payload with null bank account`() {
        //Given
        val payload = fullPayload(null)
        val event = buildSnsEvent(payload)

        //When
        sut.handleRequest(event, context)

        //Verify
        verifyNoInteractions(supplierService)
    }

    private fun buildSnsEvent(payload: String = json.encodeToString(anySupplier().toSupplierEvent())): SNSEvent {
        return SNSEvent().withRecords(listOf(
            SNSEvent.SNSRecord().withSns(SNSEvent.SNS().withMessage((payload)))
        ))
    }

    private fun Supplier.toSupplierEvent(): SupplierEvent {
        return SupplierEvent(
            supplierId = supplierId,
            country = "in",
            bankAccount = BankAccountEvent(
                number = bankAccountNumber,
                indianFinancialSystemCode = indianFinancialSystemCode
            )
        )
    }

    private fun verifyServiceInvocation(event: SNSEvent) {
        verify(supplierService).save(json.decodeFromString<SupplierEvent>(event.records.first().sns.message).toSupplier())
    }

    private fun fullPayload(bankAccount: String? = null) = """
        {"id":1258,"name":"Coastal","legalName":"Supplier test Integration Coastal","legalId":"1234567","enabled":true,"phone":"34 610929464","avatar":"258d1e79-d96c-4717-a66a-44f5830e46fb.png","country":"$country","timezone":"Africa/Nairobi","users":[],"state":null,"wabipayUsername":null,"rating":null,"bankAccount":${if(bankAccount != null) "$bankAccount" else "null"},"averageRating":null}
    """.trimIndent()

}

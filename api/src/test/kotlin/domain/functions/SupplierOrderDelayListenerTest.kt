package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import configuration.Configuration
import configuration.TestConfiguration
import domain.model.SupplierOrderDelayEvent
import domain.services.SupplierOrderDelayService
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
internal class SupplierOrderDelayListenerTest {

    private lateinit var configuration: Configuration
    private lateinit var json: Json
    private lateinit var service: SupplierOrderDelayService
    private val context: Context = mock()
    private lateinit var sut: SupplierOrderDelayListener

    @BeforeEach
    fun setUp() {
        configuration = TestConfiguration.mockedInstance()
        json = configuration.jsonMapper
        service = configuration.supplierOrderDelayService
        sut = SupplierOrderDelayListener(json, service)
    }

    private fun anySupplierOrderDelayEvent() = SupplierOrderDelayEvent(
        supplierOrderId = 77L,
        delay = false,
        delayTime = 77
    )

    @Test
    fun `can handle valid event`() {
        val event = buildSnsEvent()
        sut.handleRequest(event, context)
        verifyServiceInvocation(event)
    }

    @Test
    fun `propagates exception when supplier service fails`() {
        val event = buildSnsEvent()
        whenever(service.save(any())).thenThrow(RuntimeException())

        assertFailsWith<RuntimeException> {
            sut.handleRequest(event, context)
        }

        verifyServiceInvocation(event)
    }

    @Test
    fun `propagates the deserialization exception when sns message is not a supplier order delay event`() {
        val event = json.encodeToString(randomString()).let {
            SNSEvent().withRecords(
                listOf(
                    SNSEvent.SNSRecord().withSns(SNSEvent.SNS().withMessage((it)))
                )
            )
        }
        assertFailsWith<SerializationException> { sut.handleRequest(event, context) }
        verifyNoInteractions(service)
    }

    private fun buildSnsEvent(payload: String = json.encodeToString(anySupplierOrderDelayEvent())): SNSEvent {
        return SNSEvent().withRecords(
            listOf(
                SNSEvent.SNSRecord().withSns(SNSEvent.SNS().withMessage((payload)))
            )
        )
    }

    private fun verifyServiceInvocation(event: SNSEvent) {
        verify(service).save(json.decodeFromString(event.records.first().sns.message))
    }
}

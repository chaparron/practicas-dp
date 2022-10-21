package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import configuration.Configuration
import configuration.TestConfiguration
import domain.model.SupplierOrderDelayEvent
import domain.services.SupplierOrderDelayService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.*

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
        sut = configuration.supplierOrderDelayListener
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

package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import configuration.MainConfiguration
import domain.model.SupplierOrderDelayEvent
import domain.services.SupplierOrderDelayService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
internal class SupplierOrderDelayListenerMockitoTest {


    private val service: SupplierOrderDelayService = mock()
    private val context: Context = mock()
    private val json: Json = MainConfiguration.jsonMapper

    private val sut = SupplierOrderDelayListener(
        jsonMapper = MainConfiguration.jsonMapper,
        supplierOrderDelayService = service
    )

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

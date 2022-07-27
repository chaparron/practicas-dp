package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import domain.model.BankAccount
import domain.model.SupplierEvent
import domain.services.BankAccountService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class BankAccountListenerFunction(
    private val jsonMapper: Json,
    val bankAccountService: BankAccountService
) : RequestHandler<SNSEvent, Unit> {

    companion object {
        private val logger = LoggerFactory.getLogger(BankAccountListenerFunction::class.java)
    }

    override fun handleRequest(event: SNSEvent, context: Context) {
        event.records.forEach { record ->
            record.apply {
                logger.info("Received SNS message: {}", record)
            }.let {
                deserialize(it.sns.message)
            }.let {
                bankAccountService.save(it).also { response ->
                    logger.info("BankAccount saved {}", response)
                }
            }
        }
    }

    private fun deserialize(message: String): BankAccount {
        return jsonMapper.decodeFromString<SupplierEvent>(message).toBankAccount()
    }
}

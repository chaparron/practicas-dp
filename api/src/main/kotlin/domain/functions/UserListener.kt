package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import configuration.MainConfiguration
import domain.model.UserEvent
import domain.model.UserValidator
import domain.services.UserService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class UserListener (
    private val jsonMapper: Json = MainConfiguration.jsonMapper,
    private val service: UserService
) : RequestHandler<SNSEvent, Unit> {

    companion object {
        private val logger = LoggerFactory.getLogger(UserListener::class.java)
        private val validator = UserValidator()
    }

    override fun handleRequest(event: SNSEvent, context: Context) {
        logger.info("Event: $event")
        event.records.forEach{ record ->
            record.apply {
                logger.info("Received SNS message: {}", record)
            }.let {
                deserialize(it.sns.message)
            }.let {
                it.doHandle(validator) { event ->
                    service.get(event.userId).also { response ->
                        logger.info("User selected {}", response)
                    }
                }
            }
        }
    }

    private fun deserialize(message: String): UserEvent {
        return jsonMapper.decodeFromString(message)
    }
}

package domain.services.state

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

class JwtDecoder(private val token: String) {

    private val jsonMapper: Json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val JWT_DELIMITER = "."
    }

    fun getStateField(): String = jsonMapper.decodeFromString<Token>(
        String(Base64.getDecoder().decode(token.split(JWT_DELIMITER)[1]))
    ).state
}

@Serializable
data class Token(
    val state: String
)

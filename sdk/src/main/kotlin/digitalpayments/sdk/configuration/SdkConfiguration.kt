package digitalpayments.sdk.configuration

import com.wabi2b.serializers.BigDecimalToFloatSerializer
import com.wabi2b.serializers.InstantSerializer
import com.wabi2b.serializers.URISerializer
import com.wabi2b.serializers.UUIDStringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

class SdkConfiguration {

    companion object {
        val jsonMapper: Json
            get() = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
                serializersModule = SerializersModule {
                    contextual(InstantSerializer)
                    contextual(UUIDStringSerializer)
                    contextual(BigDecimalToFloatSerializer)
                    contextual(URISerializer)
                }
            }
    }
}

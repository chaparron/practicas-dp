package configuration

import adapters.repositories.BankAccountRepository
import adapters.repositories.DynamoDBBankAccountRepository
import adapters.rest.validations.Security
import com.wabi2b.jpmc.sdk.security.cipher.aes.encrypt.AesEncrypterService
import com.wabi2b.jpmc.sdk.security.hash.sha256.DigestHashCalculator
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import com.wabi2b.serializers.BigDecimalToFloatSerializer
import com.wabi2b.serializers.InstantSerializer
import com.wabi2b.serializers.URISerializer
import com.wabi2b.serializers.UUIDStringSerializer
import domain.functions.BankAccountListenerFunction
import domain.services.BankAccountService
import domain.services.DefaultBankAccountService
import domain.services.SaleInformationService
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.slf4j.LoggerFactory
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

object MainConfiguration : Configuration {

    private val logger = LoggerFactory.getLogger(MainConfiguration::class.java)

    override val security: Security by lazy { Security() }

    override val jsonMapper: Json by lazy {
        Json {
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

    override val saleInformationService: SaleInformationService by lazy {
        with(EnvironmentVariable.jpmcConfiguration()) {
            SaleInformationService(
                saleServiceSdk = SaleService(
                    hashCalculator = DigestHashCalculator(this.sha256HashKey), //TODO this key must be in a Secret
                    encrypter = AesEncrypterService(this.aesEncryptionKey) //TODO this key must be in a Secret
                ),
                configuration = this
            )
        }
    }

    override val jpmcStateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig by lazy {
        EnvironmentVariable.jpmcStateValidationConfig()
    }

    override val bankAccountListenerFunction: BankAccountListenerFunction by lazy {
        BankAccountListenerFunction(
            jsonMapper = jsonMapper,
            bankAccountService = bankAccountService
        )
    }

    private val bankAccountService: BankAccountService by lazy {
        DefaultBankAccountService(
            bankAccountRepository = bankAccountRepository
        )
    }

    private val bankAccountRepository: BankAccountRepository by lazy {
        DynamoDBBankAccountRepository(
            dynamoDbClient = dynamoDbClient,
            tableName = EnvironmentVariable.BANK_ACCOUNT_TABLE.get()
        )
    }

    private val dynamoDbClient: DynamoDbClient by lazy {
        logger.info("Initializing DynamoDbClient")
        DynamoDbClient
            .builder()
            .httpClient(
                UrlConnectionHttpClient
                    .builder()
                    .build()
            )
            .build()
    }
}

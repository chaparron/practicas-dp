package configuration

import adapters.repositories.jpmc.DynamoDbJpmcPaymentRepository
import adapters.repositories.jpmc.JpmcPaymentRepository
import adapters.repositories.supplier.DynamoDBSupplierRepository
import adapters.repositories.supplier.SupplierRepository
import adapters.rest.validations.Security
import com.wabi2b.jpmc.sdk.security.cipher.aes.decrypt.AesDecrypterService
import com.wabi2b.jpmc.sdk.security.cipher.aes.encrypt.AesEncrypterService
import com.wabi2b.jpmc.sdk.security.hash.sha256.DigestHashCalculator
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import com.wabi2b.serializers.BigDecimalSerializer
import com.wabi2b.serializers.BigDecimalToFloatSerializer
import com.wabi2b.serializers.InstantSerializer
import com.wabi2b.serializers.URISerializer
import com.wabi2b.serializers.UUIDStringSerializer
import configuration.EnvironmentVariable.*
import domain.functions.SupplierListenerFunction
import domain.services.*
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
import domain.services.providers.jpmc.JpmProviderService
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.slf4j.LoggerFactory
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import wabi2b.sdk.api.HttpWabi2bSdk
import wabi2b.sdk.api.Wabi2bSdk
import java.net.URI
import java.time.Clock
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.sdk.client.impl.WabiPaymentSdk
import wabi2b.sdk.customers.customer.CustomersSdk
import wabi2b.sdk.customers.customer.HttpCustomersSdk


object MainConfiguration : Configuration {

    private val logger = LoggerFactory.getLogger(MainConfiguration::class.java)

    private val security: Security by lazy { Security() }

    //aws clients
    private lateinit var credentialsProvider: EnvironmentVariableCredentialsProvider
    private lateinit var httpClient: SdkHttpClient
    private lateinit var regionValue: Region

    override val jsonMapper: Json by lazy {
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                contextual(InstantSerializer)
                contextual(UUIDStringSerializer)
                contextual(URISerializer)
                contextual(BigDecimalSerializer)
            }
        }
    }

    override val createPaymentService: CreatePaymentService by lazy {
        with(EnvironmentVariable.jpmcConfiguration()) {
            CreatePaymentService(
                saleServiceSdk = SaleService(
                    hashCalculator = DigestHashCalculator(this.sha256HashKey), //TODO this key must be in a Secret
                    encrypter = AesEncrypterService(this.aesEncryptionKey) //TODO this key must be in a Secret
                ),
                configuration = this,
                jpmcRepository = jpmcPaymentRepository,
                tokenProvider = wabi2bTokenProvider,
                paymentSdk = paymentSdk,
                paymentExpirationService = paymentExpirationService
            )
        }
    }

    override val paymentExpirationService: PaymentExpirationService by lazy {
        DefaultPaymentExpirationService(
            sqsClient = sqsClient(),
            delaySeconds = PAYMENT_EXPIRATION_DELAY_IN_SECONDS.get().toInt(),
            queueUrl = PAYMENT_EXPIRATION_QUEUE_URL.get(),
            wabiPaymentAsyncNotificationSdk = wabiPaymentAsyncNotificationSdk,
            mapper = jsonMapper
        )
    }



    private val jpmcPaymentRepository: JpmcPaymentRepository by lazy {
        DynamoDbJpmcPaymentRepository(
            dynamoDbClient = dynamoDbClient,
            tableName = JPMC_PAYMENT_TABLE.get()
        )
    }

    override val jpmcStateValidationConfig: JpmcStateValidationConfig by lazy {
        EnvironmentVariable.jpmcStateValidationConfig()
    }

    override val supplierListenerFunction: SupplierListenerFunction by lazy {
        SupplierListenerFunction(
            jsonMapper = jsonMapper,
            supplierService = supplierService
        )
    }

    override val stateValidatorService: StateValidatorService by lazy {
        StateValidatorService(
            stateValidationConfig = jpmcStateValidationConfig,
            security = security,
            customersSdk = customersSdk
        )
    }

    override val paymentProviderService: PaymentProviderService by lazy {
        PaymentProviderService(
            JpmProviderService(
                supplierService = supplierService,
                stateValidator = stateValidatorService
            )
        )
    }

    override val updatePaymentService: UpdatePaymentService by lazy {
        with(EnvironmentVariable.jpmcConfiguration()) {
            UpdatePaymentService(
                decrypter = AesDecrypterService(this.aesEncryptionKey),
                jsonMapper = jsonMapper,
                repository = jpmcPaymentRepository,
                wabiPaymentAsyncNotificationSdk = wabiPaymentAsyncNotificationSdk
            )
        }
    }

    private val supplierService: SupplierService by lazy {
        DefaultSupplierService(
            supplierRepository = supplierRepository
        )
    }

    private val supplierRepository: SupplierRepository by lazy {
        DynamoDBSupplierRepository(
            dynamoDbClient = dynamoDbClient,
            tableName = SUPPLIER_TABLE.get()
        )
    }

    override val wabi2bTokenProvider: TokenProvider by lazy {
        Wabi2bTokenProvider(
            wabi2bSdk = wabi2bSdk,
            dpClientUser = CLIENT_ID.get(),
            dpClientSecret = CLIENT_PASSWORD.get(),
            clock = clock
        )
    }

    override val wabi2bSdk: Wabi2bSdk by lazy {
        HttpWabi2bSdk
            .builder()
            .withBaseURI(URI.create(API_ROOT.get()))
            .build()
    }

    override val clock: Clock = Clock.systemUTC()


    private val dynamoDbClient: DynamoDbClient by lazy {
        logger.info("Initializing DynamoDbClient")
        DynamoDbClient
            .builder()
            .httpClient(sdkHttpClient())
            .build()
    }

    private val paymentSdk: WabiPaymentSdk by lazy {
        PAYMENTS_ROOT.get()
            .let { paymentsUrl ->
                WabiPaymentSdk(paymentsUrl).logInit("PaymentSdk initialized for: $paymentsUrl")
            }
    }

    private val wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk by lazy {
        PAYMENT_UPDATED_TOPIC_ARN.get()
            .let { topicArn ->
                WabiPaymentAsyncNotificationSdk(
                    paymentUpdatedTopicArn = topicArn
                ).also {
                    logger.trace("WabiPaymentAsyncNotificationSdk initialized for: $topicArn")
                }
            }
    }

    private val customersSdk: CustomersSdk by lazy {
        CUSTOMERS_ROOT.get()
            .let {
                url -> HttpCustomersSdk(url).logInit("CustomersSdk initialized for: $url")
            }
    }


    private fun region(): Region {
        if(!this::regionValue.isInitialized) {
            regionValue = Region.of(REGION.get()).logInit("region")
        }
        return regionValue
    }

    private fun sqsClient(): SqsClient =
        SqsClient
            .builder()
            .httpClient(sdkHttpClient())
            .credentialsProvider(environmentVariableCredentialsProvider())
            .region(region())
            .build()

    private fun sdkHttpClient(): SdkHttpClient {
        if (!this::httpClient.isInitialized) {
            httpClient = UrlConnectionHttpClient.builder().build().logInit("httpClient for type UrlConnectionHttpClient")
        }
        return httpClient
    }

    private fun environmentVariableCredentialsProvider(): EnvironmentVariableCredentialsProvider {
        if (!this::credentialsProvider.isInitialized) {
            credentialsProvider = EnvironmentVariableCredentialsProvider.create()
        }
        return credentialsProvider
    }
    private fun <T> T.logInit(message: String): T = this.also { logger.info("init {}", message) }

}

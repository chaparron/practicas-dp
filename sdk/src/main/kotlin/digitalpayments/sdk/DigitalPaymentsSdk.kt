package digitalpayments.sdk

import digitalpayments.sdk.configuration.SdkConfiguration
import digitalpayments.sdk.model.*
import domain.model.UserResponse
import kotlinx.serialization.decodeFromString
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.core.publisher.Mono
import wabi.sdk.UnexpectedResponse
import wabi.sdk.impl.CustomHttpErrorHandler
import java.net.URI
import java.util.*

interface DigitalPaymentsSdk {
    fun createPayment(createPaymentRequest: CreatePaymentRequest, accessToken: String): Mono<CreatePaymentResponse>
    fun updatePayment(updatePaymentRequest: UpdatePaymentRequest, accessToken: String): Mono<UpdatePaymentResponse>
    fun getPaymentProviders(supplierId: String, accessToken: String): Mono<List<Provider>>
    fun isDelayedSupplierOrder(supplierOrderId: String, accessToken: String): Mono<Boolean>
    fun getUser(userId: String, accessToken: String): Mono<UserResponse>
}

class HttpDigitalPaymentsSdk(root: URI) : DigitalPaymentsSdk {

    private val mapper = SdkConfiguration.jsonMapper

    private val webClient: WebClient = WebClient.builder()
        .uriBuilderFactory(DefaultUriBuilderFactory(root.toString())
            .also { DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY })
        .baseUrl(Objects.requireNonNull(root.toString()))
        .build()

    private val detailedHttpErrorHandler = CustomHttpErrorHandler()

    override fun createPayment(
        createPaymentRequest: CreatePaymentRequest,
        accessToken: String
    ): Mono<CreatePaymentResponse> =
        webClient.post()
            .uri("/dp/jpmc/createPayment")
            .header(AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromObject(createPaymentRequest))
            .retrieve()
            .onStatus(HttpStatus::isError) {
                detailedHttpErrorHandler.handle(it)
            }
            .bodyToMono(String::class.java)
            .map { responseBody ->
                mapper.decodeFromString<CreatePaymentResponse>(responseBody)
            }
            .switchIfEmpty(Mono.error(UnexpectedResponse("Unexpected error retrieving payment information for $createPaymentRequest")))

    override fun updatePayment(
        updatePaymentRequest: UpdatePaymentRequest,
        accessToken: String
    ): Mono<UpdatePaymentResponse> =
        webClient.post()
            .uri("/dp/jpmc/updatePayment")
            .header(AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromObject(updatePaymentRequest))
            .retrieve()
            .onStatus(HttpStatus::isError) {
                detailedHttpErrorHandler.handle(it)
            }
            .bodyToMono(String::class.java)
            .map { responseBody ->
                mapper.decodeFromString<UpdatePaymentResponse>(responseBody)
            }
            .switchIfEmpty(Mono.error(UnexpectedResponse("Unexpected error updating payment information for $updatePaymentRequest")))

    override fun getPaymentProviders(supplierId: String, accessToken: String): Mono<List<Provider>> =
        webClient.get()
            .uri { builder ->
                builder
                    .path("/dp/paymentProviders")
                    .queryParam("supplierId", supplierId)
                    .build()
            }
            .header(AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus(HttpStatus::isError) {
                detailedHttpErrorHandler.handle(it)
            }
            .bodyToMono(String::class.java)
            .map { responseBody ->
                mapper.decodeFromString<List<Provider>>(responseBody)
            }
            .switchIfEmpty(Mono.error(UnexpectedResponse("Unexpected error retrieving payment information with supplierId $supplierId")))

    override fun isDelayedSupplierOrder(supplierOrderId: String, accessToken: String): Mono<Boolean> =
        webClient.get()
            .uri { builder ->
                builder
                    .path("/dp/supplierOrderDelay")
                    .queryParam("supplierOrderId", supplierOrderId)
                    .build()
            }
            .header(AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus(HttpStatus::isError) {
                detailedHttpErrorHandler.handle(it)
            }
            .bodyToMono(String::class.java)
            .map { responseBody ->
                responseBody.toBoolean()
            }
            .switchIfEmpty(Mono.error(UnexpectedResponse("Unexpected error retrieving payment information with supplierOrderId $supplierOrderId")))

    override fun getUser(userId: String, accessToken: String): Mono<UserResponse> =
        webClient.get()
        .uri { builder ->
            builder
                .path("/dp/user")
                .queryParam("userId", userId)
                .build()
        }
        .header(AUTHORIZATION, "Bearer $accessToken")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .onStatus(HttpStatus::isError) {
            detailedHttpErrorHandler.handle(it)
        }
        .bodyToMono(String::class.java)
        .map { responseBody ->
            mapper.decodeFromString<UserResponse>(responseBody)
        }
        .switchIfEmpty(Mono.error(UnexpectedResponse("Unexpected error retrieving payment information with supplierOrderId $userId")))
}

package digitalpayments.sdk

import digitalpayments.sdk.configuration.SdkConfiguration
import digitalpayments.sdk.model.SaleInformationResponse
import kotlinx.serialization.decodeFromString
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.core.publisher.Mono
import wabi.sdk.UnexpectedResponse
import wabi.sdk.impl.CustomHttpErrorHandler
import java.net.URI
import java.util.*

interface JpmcSdk {
    fun getSaleInformation(amount: String, accessToken: String): Mono<SaleInformationResponse>
}

class HttpJpmcSdk(root: URI) : JpmcSdk {

    private val mapper = SdkConfiguration.jsonMapper

    private val webClient: WebClient = WebClient.builder()
        .uriBuilderFactory(DefaultUriBuilderFactory(root.toString())
            .also { DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY })
        .baseUrl(Objects.requireNonNull(root.toString()))
        .build()

    private val detailedHttpErrorHandler = CustomHttpErrorHandler()

    override fun getSaleInformation(amount: String, accessToken: String): Mono<SaleInformationResponse> =
        webClient.get()
            .uri { builder ->
                builder
                    .path("/jpmc/saleInformation")
                    .queryParam("amount", amount)
                    .build()
            }
            .header(AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus(HttpStatus::isError, detailedHttpErrorHandler::handle)
            .bodyToMono(String::class.java)
            .map { responseBody ->
                mapper.decodeFromString<SaleInformationResponse>(responseBody)
            }
            .switchIfEmpty(Mono.error(UnexpectedResponse("Unexpected error retrieving payment information with amount $amount")))
}

package adapters.rest

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import wabi2b.sdk.authorizer.domain.AuthorizerSdk

fun APIGatewayProxyRequestEvent.authorizer() = AuthorizerSdk.AuthorizerBuilder().withEvent(this).build()

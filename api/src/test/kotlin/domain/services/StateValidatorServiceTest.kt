package domain.services

import adapters.rest.validations.Security
import anyCustomer
import apiGatewayEventRequest
import configuration.EnvironmentVariable
import domain.model.errors.FunctionalityNotAvailable
import domain.services.state.StateValidatorService
import kotlin.random.Random
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import randomString
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import wabi2b.dtos.customers.shared.AddressDto
import wabi2b.dtos.customers.shared.CoordinatesDto
import wabi2b.dtos.customers.shared.StateIdDto
import wabi2b.sdk.customers.customer.CustomersSdk

@ExtendWith(MockitoExtension::class)
class StateValidatorServiceTest {

    @Mock
    private lateinit var security: Security
    @Mock
    private lateinit var customersSdk: CustomersSdk
    @Mock
    private lateinit var stateValidationConfiguration: EnvironmentVariable.JpmcStateValidationConfig
    @InjectMocks
    private lateinit var sut: StateValidatorService

    companion object {
        private const val MUMBAI_STATE = "IN-MH"
        private const val EMPTY_STATE = ""
        private const val DELIVERY_ADDRESS_TYPE = "DELIVERY"
    }

    @Nested
    inner class GetState {
        @Test
        fun `given an event without token throw UnauthorizedException`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getToken(event)).thenThrow(Security.UnauthorizedException(event.toString()))


            assertThrows<Security.UnauthorizedException> {
                sut.getState(event)
            }

            verify(stateValidationConfiguration).enabled
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getToken(event)
            verifyNoInteractions(customersSdk)
        }
        @Test
        fun `given a valid event and state configuration enabled when getState then returns the state`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()
            val someAccessToken = randomString()
            val address = AddressDto(
                id = Random.nextLong(),
                state = StateIdDto(MUMBAI_STATE),
                coordinates = CoordinatesDto(Random.nextDouble(), Random.nextDouble()),
                preferred = true,
                addressType = DELIVERY_ADDRESS_TYPE,
                formatted = null
            )
            val someAddresses = listOf(address)
            val someCustomer = anyCustomer(someAddresses)

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getToken(event)).thenReturn(someAccessToken)
            whenever(customersSdk.myProfile(someAccessToken)).thenReturn(someCustomer)

            val state = sut.getState(event)

            assertEquals(MUMBAI_STATE, state)

            verify(stateValidationConfiguration).enabled
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getToken(event)
            verify(customersSdk).myProfile(someAccessToken)
        }

        @Test
        fun `given a valid event and state configuration enabled when getState then throw CustomerWithoutStateException for customer without state`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()
            val someAccessToken = randomString()
            val address = AddressDto(
                id = Random.nextLong(),
                coordinates = CoordinatesDto(Random.nextDouble(), Random.nextDouble()),
                preferred = true,
                addressType = DELIVERY_ADDRESS_TYPE,
                formatted = null,
                state = null
            )
            val someAddresses = listOf(address)
            val someCustomer = anyCustomer(someAddresses)

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getToken(event)).thenReturn(someAccessToken)
            whenever(customersSdk.myProfile(someAccessToken)).thenReturn(someCustomer)

            assertThrows<StateValidatorService.CustomerWithoutStateException> {
                sut.getState(event)
            }

            verify(stateValidationConfiguration).enabled
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getToken(event)
            verify(customersSdk).myProfile(someAccessToken)
        }

        @Test
        fun `given a valid event and state configuration enabled throws RuntimeException when retrieve a user without valid address`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()
            val someAccessToken = randomString()
            val address = AddressDto(
                id = Random.nextLong(),
                state = StateIdDto(MUMBAI_STATE),
                coordinates = CoordinatesDto(Random.nextDouble(), Random.nextDouble()),
                preferred = true,
                addressType = randomString(),
                formatted = null
            )
            val someAddresses = listOf(address)
            val someCustomer = anyCustomer(someAddresses)


            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getToken(event)).thenReturn(someAccessToken)
            whenever(customersSdk.myProfile(someAccessToken)).thenReturn(someCustomer)


            assertThrows<java.lang.RuntimeException> {
                sut.getState(event)
            }

            verify(stateValidationConfiguration).enabled
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getToken(event)
            verify(customersSdk).myProfile(someAccessToken)
        }

        @Test
        fun `given a valid event and state configuration enabled throws RuntimeException when can not retrieve customer`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()
            val someAccessToken = randomString()


            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getToken(event)).thenReturn(someAccessToken)
            whenever(customersSdk.myProfile(someAccessToken)).thenReturn(null)


            assertThrows<java.lang.RuntimeException> {
                sut.getState(event)
            }

            verify(stateValidationConfiguration).enabled
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getToken(event)
            verify(customersSdk).myProfile(someAccessToken)
        }

        @Test
        fun `given a valid event and state configuration disabled when getState then returns empty state`() {
            val event = apiGatewayEventRequest()

            whenever(stateValidationConfiguration.enabled).thenReturn(false)

            val state = sut.getState(event)

            assertEquals(EMPTY_STATE, state)

            verify(stateValidationConfiguration).enabled
            verifyNoInteractions(security)
            verifyNoInteractions(customersSdk)
        }
    }

    @Nested
    inner class ValidateState {
        @Test
        fun `given state configuration disabled when validate then returns true`() {
            whenever(stateValidationConfiguration.enabled).thenReturn(false)

            assertTrue(sut.validate(MUMBAI_STATE))

            verify(stateValidationConfiguration).enabled
            verifyNoInteractions(customersSdk)
        }

        @Test
        fun `given state configuration enabled and state is available when validate then returns true`() {
            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(stateValidationConfiguration.availableFor).thenReturn(listOf(MUMBAI_STATE))

            assertTrue(sut.validate(MUMBAI_STATE))

            verify(stateValidationConfiguration).enabled
            verify(stateValidationConfiguration).availableFor
        }

        @Test
        fun `given state configuration enabled and state is not available when validate then returns false`() {
            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(stateValidationConfiguration.availableFor).thenReturn(listOf(randomString()))

            assertFalse(sut.validate(MUMBAI_STATE))

            verify(stateValidationConfiguration).enabled
            verify(stateValidationConfiguration).availableFor
        }
    }

    @Nested
    inner class ValidateEvent {
        @Test
        fun `given a valid event and state is available with configuration enabled when validate then works`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()
            val someAccessToken = randomString()
            val address = AddressDto(
                id = Random.nextLong(),
                state = StateIdDto(MUMBAI_STATE),
                coordinates = CoordinatesDto(Random.nextDouble(), Random.nextDouble()),
                preferred = true,
                addressType = DELIVERY_ADDRESS_TYPE,
                formatted = null
            )
            val someAddresses = listOf(address)
            val someCustomer = anyCustomer(someAddresses)

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getToken(event)).thenReturn(someAccessToken)
            whenever(stateValidationConfiguration.availableFor).thenReturn(listOf(MUMBAI_STATE))
            whenever(customersSdk.myProfile(someAccessToken)).thenReturn(someCustomer)

            sut.validate(event)

            verify(stateValidationConfiguration, times(2)).enabled
            verify(stateValidationConfiguration).availableFor
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getToken(event)
            verify(customersSdk).myProfile(someAccessToken)
        }

        @Test
        fun `given a valid event and state is not available with configuration enabled when validate then throws FunctionalityNotAvailable`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()
            val someAccessToken = randomString()
            val address = AddressDto(
                id = Random.nextLong(),
                state = StateIdDto(MUMBAI_STATE),
                coordinates = CoordinatesDto(Random.nextDouble(), Random.nextDouble()),
                preferred = true,
                addressType = DELIVERY_ADDRESS_TYPE,
                formatted = null
            )
            val someAddresses = listOf(address)
            val someCustomer = anyCustomer(someAddresses)

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getToken(event)).thenReturn(someAccessToken)
            whenever(stateValidationConfiguration.availableFor).thenReturn(listOf(randomString()))
            whenever(customersSdk.myProfile(someAccessToken)).thenReturn(someCustomer)


            assertThrows<FunctionalityNotAvailable> {
                sut.validate(event)
            }

            verify(stateValidationConfiguration, times(2)).enabled
            verify(stateValidationConfiguration).availableFor
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getToken(event)
            verify(customersSdk).myProfile(someAccessToken)

        }
    }

}

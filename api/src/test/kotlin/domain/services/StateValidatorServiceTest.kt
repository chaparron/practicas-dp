package domain.services

import adapters.rest.validations.Security
import apiGatewayEventRequest
import configuration.EnvironmentVariable
import domain.model.errors.FunctionalityNotAvailable
import domain.services.state.StateValidatorService
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

@ExtendWith(MockitoExtension::class)
class StateValidatorServiceTest {

    @Mock
    private lateinit var security: Security
    @Mock
    private lateinit var stateValidationConfiguration: EnvironmentVariable.JpmcStateValidationConfig
    @InjectMocks
    private lateinit var sut: StateValidatorService

    companion object {
        private const val STATE = "IN-MH"
        private const val EMPTY_STATE = ""
    }

    @Nested
    inner class GetState {
        @Test
        fun `given a valid event and state configuration enabled when getState then returns the state`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getState()).thenReturn(STATE)

            val state = sut.getState(event)

            assertEquals(STATE, state)

            verify(stateValidationConfiguration).enabled
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getState()
        }

        @Test
        fun `given a valid event and state configuration enabled with state null when getState then throws FunctionalityNotAvailable`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getState()).thenReturn(null)

            assertThrows<FunctionalityNotAvailable> {
                sut.getState(event)
            }

            verify(stateValidationConfiguration).enabled
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getState()
        }

        @Test
        fun `given a valid event and state configuration disabled when getState then returns empty state`() {
            val event = apiGatewayEventRequest()

            whenever(stateValidationConfiguration.enabled).thenReturn(false)

            val state = sut.getState(event)

            assertEquals(EMPTY_STATE, state)

            verify(stateValidationConfiguration).enabled
            verifyNoInteractions(security)
        }
    }

    @Nested
    inner class ValidateState {
        @Test
        fun `given state configuration disabled when validate then returns true`() {
            whenever(stateValidationConfiguration.enabled).thenReturn(false)

            assertTrue(sut.validate(STATE))

            verify(stateValidationConfiguration).enabled
        }

        @Test
        fun `given state configuration enabled and state is available when validate then returns true`() {
            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(stateValidationConfiguration.availableFor).thenReturn(listOf(STATE))

            assertTrue(sut.validate(STATE))

            verify(stateValidationConfiguration).enabled
            verify(stateValidationConfiguration).availableFor
        }

        @Test
        fun `given state configuration enabled and state is not available when validate then returns false`() {
            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(stateValidationConfiguration.availableFor).thenReturn(listOf(randomString()))

            assertFalse(sut.validate(STATE))

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

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getState()).thenReturn(STATE)
            whenever(stateValidationConfiguration.availableFor).thenReturn(listOf(STATE))

            sut.validate(event)

            verify(stateValidationConfiguration, times(2)).enabled
            verify(stateValidationConfiguration).availableFor
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getState()
        }

        @Test
        fun `given a valid event and state is not available with configuration enabled when validate then throws FunctionalityNotAvailable`() {
            val event = apiGatewayEventRequest()
            val securityWrapper = mock<Security.AuthorizerWrapper>()

            whenever(stateValidationConfiguration.enabled).thenReturn(true)
            whenever(security.buildAuthorizer(event)).thenReturn(securityWrapper)
            whenever(securityWrapper.getState()).thenReturn(STATE)
            whenever(stateValidationConfiguration.availableFor).thenReturn(listOf(randomString()))

            assertThrows<FunctionalityNotAvailable> {
                sut.validate(event)
            }

            verify(stateValidationConfiguration, times(2)).enabled
            verify(stateValidationConfiguration).availableFor
            verify(security).buildAuthorizer(event)
            verify(securityWrapper).getState()
        }
    }

}

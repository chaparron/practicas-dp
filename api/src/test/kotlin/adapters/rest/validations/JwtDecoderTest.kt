package adapters.rest.validations

import domain.services.state.JwtDecoder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals

class JwtDecoderTest {

    @ParameterizedTest
    @ValueSource(strings = ["eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmV2aW91c0xvZ2luIjoxNjU4ODIwNTA4MDAwLCJ1c2VyX25hbWUiOiIrOTEtOTk5OTk4Mjk2OCIsImVudGl0eVR5cGUiOiJDVVNUT01FUiIsInNjb3BlIjpbImFsbCJdLCJ0b3MiOnsiYWNjZXB0ZWQiOjE2MjUyNTU1MzgwMDB9LCJlbnRpdHlJZCI6IjkyNyIsInN0YXRlIjoiSU4tQVAiLCJleHAiOjE2NTg4NjM3MDcsInVzZXIiOnsiaWQiOjE0ODgsInVzZXJuYW1lIjoiKzkxLTk5OTk5ODI5NjgiLCJmaXJzdE5hbWUiOiJUZXN0IHN0b3JlIiwibGFzdE5hbWUiOiJJbmRpYSIsImNvdW50cmllcyI6W3siaWQiOiJpbiIsIm5hbWUiOiJJbmRpYSJ9XX0sImF1dGhvcml0aWVzIjpbIkZFX1dFQiJdLCJqdGkiOiJhYTRmYmFkNS0zODJhLTQ5YTAtYTRmZS04ZWYwNGI1MTRlNDMiLCJjbGllbnRfaWQiOiJpbnRlcm5hbF9hcGkifQ.1N3oaNNlmV6bOhbsbTAPgTquEpvsLkyC2nM5_vKAxio"])
    fun getPayload(token: String) {
        val state = JwtDecoder(token).getStateField()
        assertEquals("IN-AP", state)
    }
}

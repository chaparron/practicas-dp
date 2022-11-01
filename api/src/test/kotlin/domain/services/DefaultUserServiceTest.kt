package domain.services

import adapters.repositories.user.UserRepository
import domain.model.Role
import domain.model.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class DefaultUserServiceTest {

    @Mock
    private lateinit var repository: UserRepository

    @InjectMocks
    private lateinit var sut: DefaultUserService

    private val anyUser = User(
        name = "Tete",
        userId = 77L,
        mail = "tete@wabi.com",
        country = "Spain",
        active = true,
        phone = "+3465432112",
        role = Role.USER,
        createdAt = "2018-10-10",
        lastLogin = "30-10-2022",
        orders = listOf("123", "456", "789")
    )

    @Test
    fun `Should return the user with a valid id`() {
        // Given
        val userId = anyUser.userId
        // When
        whenever(repository.get(userId)).thenReturn(anyUser)
        // Then
        val expected = anyUser
        val actual = sut.get(userId)
        assertEquals(expected, actual)
        // Verify
        verify(repository).get(userId)
    }

    @Test
    fun `Should save the user`() {
        // When
        whenever(repository.save(anyUser)).thenReturn(anyUser)
        // Then
        val expected = anyUser
        val actual = sut.save(anyUser)
        assertEquals(expected, actual)
        // Verify
        verify(repository).save(anyUser)
    }


}

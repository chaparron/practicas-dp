package adapters.repositories.user

import adapters.infrastructure.DynamoDbContainer
import domain.model.Role
import domain.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import wabipay.commons.dynamodb.testing.DynamoDbTestUtils
import java.net.URI
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDBUserRepositoryTest {
    private val dynamoDbTestUtils = DynamoDbTestUtils(
        DynamoDbContainer.localStack.getEndpointConfiguration(LocalStackContainer.Service.DYNAMODB).serviceEndpoint
            .let { URI.create(it) }
    )
    private var dynamoDbClient: DynamoDbClient = dynamoDbTestUtils.dynamoDb

    private lateinit var sut: DynamoDBUserRepository

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

    @BeforeAll
    fun init() {
        sut = DynamoDBUserRepository(dynamoDbClient, DynamoDbTestUtils.tableName)
        dynamoDbTestUtils.createSingleTable(DynamoDbContainer.digitalPaymentTableSchema())
    }

    @BeforeEach
    fun setup() {
        dynamoDbTestUtils.removeAll()
    }

    @Test
    fun `should retrieve the same saved supplier order delay using the same id`() {
        // Given
        val userId = anyUser.userId
        // When
        val saved = sut.save(anyUser)
        val retrieved = sut.get(userId)
        // Then
        println("\u001b[7m $saved \u001b[0m")
        println("\u001b[7m $retrieved \u001b[0m")
        assertEquals(saved, retrieved)
    }
    @Test
    fun `should retrieve user not found using non existing id`() {
        // Given
        val userId = 135L
        // Then
        assertThrows<DynamoDBUserRepository.UserNotFound> {
            sut.get(userId)
        }
    }
    @Test
    fun `should delete existing user using his id`() {
        // Given
        val saved = sut.save(anyUser)
        val retrieved = sut.get(anyUser.userId)
        val deleted = sut.delete(anyUser.userId)
        // Then
        println("\u001b[7m $saved \u001b[0m")
        println("\u001b[7m $retrieved \u001b[0m")
        println("\u001b[7m $deleted \u001b[0m")
    }

}

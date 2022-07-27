package adapters.repositories

import anyBankAccount
import adapters.infrastructure.CreateTableRequest
import adapters.infrastructure.DynamoDbContainer
import adapters.infrastructure.DynamoTestSupport
import adapters.infrastructure.DynamoTestSupport.bankAccountTable
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import randomString
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDBBankAccountRepositoryTest {

    companion object {
        @JvmStatic
        @Container
        val container: DynamoDbContainer = DynamoDbContainer()
    }

    private lateinit var dynamoDbClient: DynamoDbClient

    private lateinit var sut: DynamoDBBankAccountRepository

    @BeforeAll
    fun setUp() {
        dynamoDbClient = DynamoTestSupport.dynamoDbClient(container.endpoint())
        sut = DynamoDBBankAccountRepository(dynamoDbClient, bankAccountTable)

        CreateTableRequest(
            tableName = bankAccountTable,
            attributes = listOf(CreateTableRequest.Param(DynamoDBAttribute.PK.param)),
            pk = DynamoDBAttribute.PK.param
        ).doExecuteWith(dynamoDbClient, DynamoTestSupport::createTable)
    }

    @Test
    fun `finds saved bank account by supplierId`() {
        val saved = sut.save(anyBankAccount())

        val retrieved = sut.get(saved.supplierId)

        assertEquals(saved, retrieved)
    }

    @Test
    fun `throws BankAccountPayoutNotFound when bank account does not exist`() {
        val supplierId = randomString()

        assertFailsWith<BankAccountPayoutNotFound> {
            sut.get(supplierId)
        }
    }
}
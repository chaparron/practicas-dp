package adapters.repositories.paymentforreport

import adapters.infrastructure.DynamoDbContainer
import adapters.repositories.supplier.DynamoDBSupplierRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import wabipay.commons.dynamodb.testing.DynamoDbTestUtils
import java.net.URI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDBPaymentForReportRepositoryTest {

    private val dynamoDbTestUtils = DynamoDbTestUtils( // WTF
        DynamoDbContainer.localStack.getEndpointConfiguration(LocalStackContainer.Service.DYNAMODB).serviceEndpoint
            .let { URI.create(it) }
    )
    private var dynamoDbClient: DynamoDbClient = dynamoDbTestUtils.dynamoDb

    private lateinit var sut: DynamoDBPaymentForReportRepository

    @BeforeAll
    fun init() {
        sut = DynamoDBPaymentForReportRepository(dynamoDbClient, DynamoDbTestUtils.tableName)
        dynamoDbTestUtils.createSingleTable(DynamoDbContainer.digitalPaymentTableSchema())
    }

    @BeforeEach
    fun setup() {
        dynamoDbTestUtils.removeAll()
    }
}

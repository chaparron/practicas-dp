package adapters.repositories.paymentforreport

import adapters.infrastructure.DynamoDbContainer
import domain.model.PaymentForReport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.type.PaymentMethod
import wabipay.commons.dynamodb.testing.DynamoDbTestUtils
import java.math.BigDecimal
import java.net.URI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDBPaymentForReportRepositoryTest {

    private val dynamoDbTestUtils = DynamoDbTestUtils(
        DynamoDbContainer.localStack.getEndpointConfiguration(LocalStackContainer.Service.DYNAMODB).serviceEndpoint
            .let { URI.create(it) }
    )
    private var dynamoDbClient: DynamoDbClient = dynamoDbTestUtils.dynamoDb

    private lateinit var sut: DynamoDBPaymentForReportRepository

    private val anyPaymentForReport = PaymentForReport(
        createdAt = "now",
        reportDay = "tomorrow",
        paymentId = 77L,
        supplierOrderId = 77L,
        amount = BigDecimal(77),
        paymentOption = "installment",
        encData = "encoded Date",
        paymentType = PaymentType.DIGITAL_PAYMENT,
        paymentMethod = PaymentMethod.DIGITAL_WALLET
    )

    @BeforeAll
    fun init() {
        sut = DynamoDBPaymentForReportRepository(dynamoDbClient, DynamoDbTestUtils.tableName)
        dynamoDbTestUtils.createSingleTable(DynamoDbContainer.digitalPaymentTableSchema())
    }

    @BeforeEach
    fun setup() {
        dynamoDbTestUtils.removeAll()
    }

//    @Test
//    fun `should save and return the same Payment For Report`() {
//        val saved = sut.save(anyPaymentForReport)
//        val retrieved = sut.getOne(anyPaymentForReport.paymentId)
//        assertEquals(saved, retrieved)
//    }

    @Test
    fun `should contain Payment For Report in the list of a date`() {
        val saved = sut.save(anyPaymentForReport)
        val retrieved = sut.get(anyPaymentForReport.reportDay)
        assertTrue(retrieved.contains(saved))
    }
}

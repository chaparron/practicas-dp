package adapters.repositories.jpmc

import adapters.infrastructure.CreateTableRequest
import adapters.infrastructure.DynamoDbContainer
import adapters.infrastructure.DynamoTestSupport
import adapters.repositories.supplier.DynamoDBSupplierAttribute
import domain.model.Payment
import domain.model.PaymentStatus
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import randomString
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDbJpmcPaymentRepositoryTest {

    companion object {
        @JvmStatic
        @Container
        val container: DynamoDbContainer = DynamoDbContainer()
    }

    private lateinit var dynamoDbClient: DynamoDbClient

    private lateinit var sut: DynamoDbJpmcPaymentRepository

    @BeforeAll
    fun setUp() {
        dynamoDbClient = DynamoTestSupport.dynamoDbClient(container.endpoint())
        sut = DynamoDbJpmcPaymentRepository(dynamoDbClient, DynamoTestSupport.supplierTable)

        CreateTableRequest(
            tableName = DynamoTestSupport.supplierTable,
            attributes = listOf(
                CreateTableRequest.Param(DynamoDBSupplierAttribute.PK.param),
                CreateTableRequest.Param(DynamoDBSupplierAttribute.SK.param)),
            pk = DynamoDBSupplierAttribute.PK.param,
            sk = DynamoDBSupplierAttribute.SK.param
        ).doExecuteWith(dynamoDbClient, DynamoTestSupport::createTable)
    }

    @Test
    fun `can retrieve a payment by payment id`() {
        //given
        val payment = anySavedPayment()
        /**control payment**/ anySavedPayment()

        //when
        val actual = sut.findBy(payment.paymentId)

        //then
        assertEquals(payment, actual)
    }

    @Test
    fun `fails on not found payment`() {
        //given
        /**control payment**/ anySavedPayment()

        //
        val paymentId = randomString()

        //then
        assertFailsWith<PaymentNotFound> {
            sut.findBy(paymentId)
        }
    }

    private fun anySavedPayment(): Payment {
        return sut.save(anyPayment())
    }

    private fun anyPayment(created: Instant = Instant.now()): Payment = Payment(
        supplierOrderId = randomString(),
        paymentId = randomString(),
        amount = "161",
        status = PaymentStatus.IN_PROGRESS,
        created = created
    )
}

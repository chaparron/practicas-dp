package adapters.repositories.jpmc

import adapters.infrastructure.DynamoDbContainer
import adapters.infrastructure.DynamoDbContainer.digitalPaymentTableSchema
import domain.model.Payment
import domain.model.PaymentForSave
import domain.model.PaymentForUpdate
import domain.model.PaymentStatus
import domain.model.errors.PaymentNotFound
import domain.model.errors.UpdatePaymentException
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.localstack.LocalStackContainer
import randomBigDecimal
import randomLong
import randomString
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import wabipay.commons.dynamodb.testing.DynamoDbTestUtils
import java.net.URI
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDbJpmcPaymentRepositoryTest {
    private val dynamoDbTestUtils = DynamoDbTestUtils(
        DynamoDbContainer.localStack.getEndpointConfiguration(LocalStackContainer.Service.DYNAMODB).serviceEndpoint
            .let { URI.create(it) }
    )
    private var dynamoDbClient: DynamoDbClient = dynamoDbTestUtils.dynamoDb

    private lateinit var sut: DynamoDbJpmcPaymentRepository

    @BeforeAll
    fun init() {
        sut = DynamoDbJpmcPaymentRepository(dynamoDbClient, DynamoDbTestUtils.tableName)
        dynamoDbTestUtils.createSingleTable(digitalPaymentTableSchema())
    }

    @BeforeEach
    fun setup(){
        dynamoDbTestUtils.removeAll()
    }

    @Test
    fun `can retrieve a payment by payment id`() {
        //given
        val payment = anySavedPayment()
        /**control payment**/ anySavedPayment()

        val expectedPayment = Payment(
            supplierOrderId = payment.supplierOrderId,
            paymentId = payment.paymentId,
            amount = payment.amount,
            invoiceId = payment.invoiceId,
            status = payment.status,
            createdAt = payment.createdAt,
            lastUpdatedAt = payment.lastUpdatedAt
        )

        //when
        val actual = sut.findBy(payment.paymentId.toString())

        //then
        assertEquals(expectedPayment, actual)
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

    @Test
    fun `can update a payment by id`() {
        val anyPayment = anyPaymentForSave()
        val updatedPayment = PaymentForUpdate(
            paymentId = anyPayment.paymentId,
            paymentOption = randomString(),
            responseCode = randomString(),
            message = randomString(),
            encData = randomString(),
            status = PaymentStatus.PAID,
            lastUpdatedAt = Instant.now().toString()
        )

        val expectedPayment = Payment(
            supplierOrderId = anyPayment.supplierOrderId,
            paymentId = anyPayment.paymentId,
            amount = anyPayment.amount,
            status = updatedPayment.status,
            invoiceId = anyPayment.invoiceId,
            createdAt = anyPayment.createdAt,
            lastUpdatedAt = updatedPayment.lastUpdatedAt,
            paymentOption = updatedPayment.paymentOption,
            responseCode = updatedPayment.responseCode,
            message = updatedPayment.message,
            encData = updatedPayment.encData
        )

        sut.save(anyPayment)
        sut.update(updatedPayment)
        val actual = sut.findBy(anyPayment.paymentId.toString())

        assertEquals(expectedPayment, actual)
    }

    @Test
    fun `fails with UpdatePaymentException on error at update`() {
        val payment = anyPaymentForUpdate()

        assertFailsWith<UpdatePaymentException> {
            sut.update(payment)
        }
    }


    private fun anySavedPayment(): PaymentForSave {
        return sut.save(anyPaymentForSave())
    }

    private fun anyPaymentForSave(): PaymentForSave = PaymentForSave(
        supplierOrderId = randomLong(),
        paymentId = randomLong(),
        amount = randomBigDecimal(),
        status = PaymentStatus.IN_PROGRESS,
        invoiceId = randomString(),
        createdAt = Instant.now().toString(),
        lastUpdatedAt = Instant.now().toString()
    )

    private fun anyPaymentForUpdate(): PaymentForUpdate = PaymentForUpdate(
        paymentId = randomLong(),
        paymentOption = randomString(),
        responseCode = randomString(),
        message = randomString(),
        encData = randomString(),
        status = PaymentStatus.PAID,
        lastUpdatedAt = Instant.now().toString()
    )
}

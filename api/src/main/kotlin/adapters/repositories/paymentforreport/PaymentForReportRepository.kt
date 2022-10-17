package adapters.repositories.paymentforreport

import adapters.repositories.jpmc.DynamoDBJpmcAttribute
import adapters.repositories.supplier.DynamoDBSupplierAttribute
import domain.model.PaymentForReport
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.type.PaymentMethod
import java.math.BigDecimal
import java.util.*

interface PaymentForReportRepository {
    fun save(paymentForReport: PaymentForReport): PaymentForReport
    fun getOne(paymentId: Long): PaymentForReport
    fun get(reportDate: String): List<PaymentForReport>
}

class DynamoDBPaymentForReportRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : PaymentForReportRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDBPaymentForReportRepository::class.java)
        private const val pkValuePrefix = "paymentForReport"
    }

    override fun save(paymentForReport: PaymentForReport): PaymentForReport {
        return dynamoDbClient.putItem {
            logger.trace("Saving PaymentForReport $paymentForReport")
            it.tableName(tableName).item(paymentForReport.asDynamoItem())
        }.let {
            logger.trace("Saved PaymentForReport $paymentForReport")
            paymentForReport
        }
    }

    override fun getOne(paymentId: Long): PaymentForReport =
        dynamoDbClient.getItem {
            it.tableName(tableName).key(paymentId.asGetItemKey())
        }.let { response ->
            response.takeIf { it.hasItem() }?.item()?.asPaymentForReport() ?: throw PaymentForReportNotFound(paymentId)
        }

    override fun get(reportDate: String): List<PaymentForReport> {
        return dynamoDbClient.queryPaginator {
            it
                .tableName(tableName)
                .keyConditionExpression("${DynamoDBJpmcAttribute.PK.param} = :pk")
                .expressionAttributeValues(
                    mapOf(
                        ":pk" to (pkValuePrefix + reportDate).toAttributeValue()
                    )
                )
        }.items().map {
            it.asPaymentForReport()
        }
    }

    private fun PaymentForReport.asDynamoItem() = mapOf(
        DynamoDBPaymentForReportAttribute.PK.param to "$pkValuePrefix${this.paymentId}".toAttributeValue(),
        DynamoDBPaymentForReportAttribute.SK.param to this.paymentId.toString().toAttributeValue(),
        DynamoDBPaymentForReportAttribute.CA.param to this.createdAt.toAttributeValue(),
        DynamoDBPaymentForReportAttribute.RD.param to this.reportDay.toAttributeValue(),
        DynamoDBPaymentForReportAttribute.ED.param to this.encData.toAttributeValue(),
        DynamoDBPaymentForReportAttribute.SI.param to this.supplierOrderId.toString().toAttributeValue(),
        DynamoDBPaymentForReportAttribute.A.param to this.amount.toString().toAttributeValue(),
        DynamoDBPaymentForReportAttribute.PO.param to this.paymentOption.toAttributeValue(),
        DynamoDBPaymentForReportAttribute.PT.param to this.paymentType.toString().toAttributeValue(),
        DynamoDBPaymentForReportAttribute.PM.param to this.paymentMethod.toString().toAttributeValue(),
    )

    private fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()

    private fun Long.asGetItemKey() = mapOf(
        DynamoDBSupplierAttribute.PK.param to "$pkValuePrefix$this".toAttributeValue(),
        DynamoDBSupplierAttribute.SK.param to this.toString().toAttributeValue()
    )

    private fun Map<String, AttributeValue>.asPaymentForReport() =
        PaymentForReport(
            createdAt = this.getValue(DynamoDBPaymentForReportAttribute.CA.param).s(),
            reportDay = this.getValue(DynamoDBPaymentForReportAttribute.RD.param).s(),
            paymentId = this.getValue(DynamoDBPaymentForReportAttribute.PK.param).s().toLong(),
            supplierOrderId = this[DynamoDBPaymentForReportAttribute.PK.param]?.s()!!.toLong(), // Paprobar
            amount = BigDecimal(this.getValue(DynamoDBPaymentForReportAttribute.A.param).s()),
            paymentOption = this.getValue(DynamoDBPaymentForReportAttribute.PO.param).s(),
            encData = this.getValue(DynamoDBPaymentForReportAttribute.ED.param).s(),
//            paymentType = this.getValue(DynamoDBPaymentForReportAttribute.PT.param),
            paymentType = PaymentType.DIGITAL_PAYMENT,
//            paymentMethod = this.getValue(DynamoDBPaymentForReportAttribute.PM.param),
            paymentMethod = PaymentMethod.DIGITAL_WALLET
        )

    data class PaymentForReportNotFound(val paymentId: Long): RuntimeException("Cannot find any paymentForReport for $paymentId")
}

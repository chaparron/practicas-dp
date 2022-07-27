package adapters.repositories

import domain.model.BankAccount
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import toAttributeValue

interface BankAccountRepository {
    fun save(bankAccount: BankAccount): BankAccount
    fun get(supplierId: String): BankAccount
}

class DynamoDBBankAccountRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : BankAccountRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDBBankAccountRepository::class.java)
    }

    override fun save(bankAccount: BankAccount): BankAccount {
        return dynamoDbClient.putItem {
            logger.trace("Saving item for Bank Account $bankAccount")
            it.tableName(tableName).item(bankAccount.asDynamoItem())
        }.let {
            logger.trace("Saved Bank Account $bankAccount")
            bankAccount
        }
    }

    override fun get(supplierId: String): BankAccount =
        dynamoDbClient.getItem {
            it.tableName(tableName).key(supplierId.asPkAttribute())
        }.let {  response ->
            response.takeIf { it.hasItem() }?.item()?.asBankAccount() ?: throw BankAccountPayoutNotFound(supplierId)
        }

    private fun BankAccount.asDynamoItem() = mapOf(
        DynamoDBAttribute.PK.param to this.supplierId.toAttributeValue(),
        DynamoDBAttribute.SI.param to this.supplierId.toAttributeValue(),
        DynamoDBAttribute.N.param to this.number.toAttributeValue(),
        DynamoDBAttribute.C.param to this.indianFinancialSystemCode.toAttributeValue()
    )

    private fun String.asPkAttribute() = mapOf(
        DynamoDBAttribute.PK.param to this.toAttributeValue()
    )

    private fun Map<String, AttributeValue>.asBankAccount() =
        BankAccount(
            supplierId = this[DynamoDBAttribute.SI.param]?.s()!!,
            number = this[DynamoDBAttribute.N.param]?.s()!!,
            indianFinancialSystemCode = this[DynamoDBAttribute.C.param]?.s()!!
        )
}

data class BankAccountPayoutNotFound(val supplierId: String): RuntimeException("Cannot find any Bank Account for $supplierId")

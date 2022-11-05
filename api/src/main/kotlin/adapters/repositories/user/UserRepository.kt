package adapters.repositories.user

import domain.model.Role
import domain.model.User
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import toAttributeValue

interface UserRepository {
    fun save(user: User): User
    fun get(id: Long): User
    fun delete(id: Long)
    fun update(user: User): User
}

class DynamoDBUserRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
): UserRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDBUserRepository::class.java)
        private const val pkValuePrefix = "user#"
    }

    // https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/WorkingWithItems.html

    override fun save(user: User): User {
        return dynamoDbClient.putItem {
            logger.trace("Saving user $user")
            it.tableName(tableName).item(user.asDynamoItem())
        }.let {
            logger.trace("Saved user $user")
            user
        }
    }

    override fun get(id: Long): User {
        return dynamoDbClient.getItem {
            logger.trace("getting user with id $id")
            it.tableName(tableName).key(id.asGetItemKey())
        }.let { response ->
            response.takeIf { it.hasItem() }?.item()?.asUser() ?: throw UserNotFound(id)
        }
    }

    override fun delete(id: Long) {
        dynamoDbClient.deleteItem {
            logger.trace("deleting user with id $id")
            it.tableName(tableName).key(id.asGetItemKey())
        }
    }

    override fun update(user: User): User {
        return dynamoDbClient.updateItem {
            logger.trace("Updating user $user")
            it
                .tableName(tableName)
                .key(user.userId.asGetItemKey())
                .updateExpression(
                    "SET " +
//                            "${DynamoDBUserAttribute.PK.param} = :userId," +
                            "${DynamoDBUserAttribute.SK.param} = :userId," +
                            "${DynamoDBUserAttribute.N.param} = :name," +
                            "${DynamoDBUserAttribute.M.param} = :mail," +
                            "${DynamoDBUserAttribute.C.param} = :country," +
                            "${DynamoDBUserAttribute.A.param} = :active," +
                            "${DynamoDBUserAttribute.P.param} = :phone," +
                            "${DynamoDBUserAttribute.R.param} = :role," +
                            "${DynamoDBUserAttribute.CA.param} = :createdAt," +
                            "${DynamoDBUserAttribute.LL.param} = :lastLogin," +
                            "${DynamoDBUserAttribute.O.param} = :orders,"
                )
                .expressionAttributeValues(mapOf(
//                    ":userId" to user."$pkValuePrefix${user.userId}".toAttributeValue(),
                    ":userId" to user.userId.toAttributeValue(),
                    ":name" to user.name.toAttributeValue(),
                    ":mail" to user.mail.toAttributeValue(),
                    ":country" to user.country.toAttributeValue(),
                    ":active" to user.active.toString().toAttributeValue(),
                    ":phone" to user.phone.toAttributeValue(),
                    ":role" to user.role.toString().toAttributeValue(),
                    ":createdAt" to user.createdAt.toAttributeValue(),
                    ":lastLogin" to user.lastLogin.toAttributeValue(),
                    ":orders" to user.orders.joinToString().toAttributeValue(),
                ))
        }.let {
            logger.trace("Updated user $user")
            user
        }
    }

//    override fun save(supplier: Supplier): Supplier {
//        return dynamoDbClient.updateItem {
//            logger.trace("Saving Supplier $supplier")
//            it
//                .tableName(tableName)
//                .key(supplier.supplierId.asDynamoKey())
//                .updateExpression(
//                    "SET " +
//                            "${DynamoDBSupplierAttribute.SI.param} = :supplierId," +
//                            "${DynamoDBSupplierAttribute.C.param} = :code," +
//                            "${DynamoDBSupplierAttribute.N.param} = :bankAccountNumber," +
//                            "${DynamoDBSupplierAttribute.LN.param} = :legalName"
//                )
//                .expressionAttributeValues(mapOf(
//                    ":supplierId" to supplier.supplierId.toAttributeValue(),
//                    ":code" to supplier.indianFinancialSystemCode.toAttributeValue(),
//                    ":bankAccountNumber" to supplier.bankAccountNumber.toAttributeValue(),
//                    ":legalName" to supplier.legalName.toAttributeValue()
//                ))
//        }.let {
//            logger.trace("Saved Supplier $supplier")
//            supplier
//        }
//    }

    private fun Long.asGetItemKey() = mapOf(
        DynamoDBUserAttribute.PK.param to "$pkValuePrefix$this".toAttributeValue(),
        DynamoDBUserAttribute.SK.param to this.toString().toAttributeValue()
    )

    private fun User.asDynamoItem() = mapOf(
        DynamoDBUserAttribute.PK.param to "$pkValuePrefix$userId".toAttributeValue(),
        DynamoDBUserAttribute.SK.param to userId.toAttributeValue(),
        DynamoDBUserAttribute.N.param to name.toAttributeValue(),
        DynamoDBUserAttribute.M.param to mail.toAttributeValue(),
        DynamoDBUserAttribute.C.param to country.toAttributeValue(),
        DynamoDBUserAttribute.A.param to active.toString().toAttributeValue(),
        DynamoDBUserAttribute.P.param to phone.toAttributeValue(),
        DynamoDBUserAttribute.R.param to role.toString().toAttributeValue(),
        DynamoDBUserAttribute.CA.param to createdAt.toAttributeValue(),
        DynamoDBUserAttribute.LL.param to lastLogin.toAttributeValue(),
        DynamoDBUserAttribute.O.param to orders.joinToString().toAttributeValue(),
    )

    private fun Map<String, AttributeValue>.asUser() =
        User(
            name = this.getValue(DynamoDBUserAttribute.N.param).s(),
            userId = this.getValue(DynamoDBUserAttribute.SK.param).s().toLong(),
            mail = this.getValue(DynamoDBUserAttribute.M.param).s(),
            country = this.getValue(DynamoDBUserAttribute.C.param).s(),
            active = (this.getValue(DynamoDBUserAttribute.A.param).s()).toBoolean(),
            phone = this.getValue(DynamoDBUserAttribute.P.param).s(),
            role = Role.valueOf(this.getValue(DynamoDBUserAttribute.R.param).s()),
            createdAt = this.getValue(DynamoDBUserAttribute.CA.param).s(),
            lastLogin = this.getValue(DynamoDBUserAttribute.LL.param).s(),
            orders = this.getValue(DynamoDBUserAttribute.O.param).s().split(", ")
        )

    data class UserNotFound(val userId: Long) :
        RuntimeException("Cannot find user with id $userId")
}

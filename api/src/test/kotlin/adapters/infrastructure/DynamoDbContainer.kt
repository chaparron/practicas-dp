package adapters.infrastructure

import adapters.repositories.supplier.DynamoDBSupplierAttribute
import org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import wabipay.commons.dynamodb.testing.TableSchema
import wabipay.commons.localstack.LocalStackContainer

object DynamoDbContainer: LocalStackContainer(DYNAMODB){
    fun digitalPaymentTableSchema() = TableSchema(
        attributeDefinition = listOf(
            AttributeDefinition.builder()
                .attributeName(DynamoDBSupplierAttribute.PK.param)
                .attributeType(ScalarAttributeType.S)
                .build(),
            AttributeDefinition.builder()
                .attributeName(DynamoDBSupplierAttribute.SK.param)
                .attributeType(ScalarAttributeType.S)
                .build()),
        keySchema = listOf(
            KeySchemaElement.builder()
                .attributeName(DynamoDBSupplierAttribute.PK.param)
                .keyType(KeyType.HASH)
                .build(),
            KeySchemaElement.builder()
                .attributeName(DynamoDBSupplierAttribute.SK.param)
                .keyType(KeyType.RANGE)
                .build()
        )
    )
}

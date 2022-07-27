package adapters.infrastructure

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.net.URI
import java.util.concurrent.TimeUnit

object DynamoTestSupport {

    const val bankAccountTable = "bankAccountTable"

    internal fun dynamoDbClient(endpoint: String): DynamoDbClient =
        DynamoDbClient
            .builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")))
            .httpClient(UrlConnectionHttpClient.builder().build())
            .region(Region.US_EAST_2)
            .build()

    internal fun createTable(dynamoDbClient: DynamoDbClient, request: CreateTableRequest) {
        try {
            dynamoDbClient.createTable { it
                .tableName(request.tableName)
                .attributeDefinitions(
                    request.attributes.map { param ->
                        val type = if(param.isString) ScalarAttributeType.S else ScalarAttributeType.N
                        AttributeDefinition.builder().attributeName(param.name).attributeType(type).build()
                    }
                ).billingMode(BillingMode.PAY_PER_REQUEST)
                .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(100).writeCapacityUnits(100).build())
                .keySchema(
                    request.toKeySchemaElement()
                ).apply {
                    if(request.gsi.isNotEmpty()) {
                        val indexes = request.gsi.map { gsi ->
                            val key = KeySchemaElement.builder().attributeName(gsi.hash).keyType(KeyType.HASH).build()
                            val range = gsi.range?.let { range ->
                                KeySchemaElement.builder().attributeName(range).keyType(KeyType.RANGE).build()
                            }
                            buildGlobalSecondaryIndex(
                                gsi.indexName,
                                gsi.onlyKeys.takeIf { i -> i }?.let{ ProjectionType.KEYS_ONLY} ?: ProjectionType.ALL,
                                *listOfNotNull(key, range).toTypedArray()
                            )
                        }
                        globalSecondaryIndexes(
                            indexes
                        )
                    }
                    if(request.lsi.isNotEmpty()) {
                        val indexes = request.lsi.map { lsi ->
                            val key = KeySchemaElement.builder().attributeName(lsi.hash).keyType(KeyType.HASH).build()
                            val range = lsi.range?.let { range ->
                                KeySchemaElement.builder().attributeName(range).keyType(KeyType.RANGE).build()
                            }
                            buildLocalSecondaryIndex(
                                lsi.indexName,
                                *listOfNotNull(key, range).toTypedArray()
                            )
                        }
                        localSecondaryIndexes(
                            indexes
                        )
                    }
                }.build()
            }.waitForActive(dynamoDbClient)
        } catch (ignore: ResourceInUseException) {
        }
    }

    private fun CreateTableRequest.toKeySchemaElement(): List<KeySchemaElement> {
        val pkElement = KeySchemaElement.builder().attributeName(pk).keyType(KeyType.HASH).build()
        val skElement = sk?.let {
            KeySchemaElement.builder().attributeName(it).keyType(KeyType.RANGE).build()
        }
        return listOfNotNull(pkElement, skElement)
    }

    private fun buildGlobalSecondaryIndex(indexName: String, projectionType: ProjectionType = ProjectionType.KEYS_ONLY, vararg keySchemaElements: KeySchemaElement) = Projection.builder()
        .projectionType(projectionType)
        .build().let {
            GlobalSecondaryIndex.builder()
                .indexName(indexName)
                .keySchema(keySchemaElements.asList())
                .projection(it)
                .build()
        }

    private fun buildLocalSecondaryIndex(indexName: String, vararg keySchemaElements: KeySchemaElement ) = Projection.builder()
        .projectionType(ProjectionType.ALL)
        .build().let {
            LocalSecondaryIndex.builder()
                .indexName(indexName)
                .keySchema(keySchemaElements.asList())
                .projection(it)
                .build()
        }

    private fun CreateTableResponse.waitForActive(client: DynamoDbClient) {
        var status = tableDescription().tableStatus()

        while (status != TableStatus.ACTIVE) {
            TimeUnit.MILLISECONDS.sleep(200)
            try {
                status = client.describeTable { it.tableName(tableDescription().tableName()) }.table().tableStatus()
            } catch (ignore: ResourceNotFoundException) {
            }
        }
    }
}

data class CreateTableRequest(
    val tableName: String,
    val attributes: List<Param>,
    val pk: String,
    val sk: String? = null,
    val gsi: List<CreateIndexRequest> = emptyList(),
    val lsi: List<CreateIndexRequest> = emptyList()
) {
    fun doExecuteWith(client: DynamoDbClient, block: (DynamoDbClient, CreateTableRequest) -> Unit) {
        block(client, this)
    }

    data class CreateIndexRequest(
        val indexName: String,
        val hash: String,
        val range: String? = null,
        val onlyKeys: Boolean = false
    )

    data class Param(
        val name: String,
        val isString: Boolean = true
    )
}

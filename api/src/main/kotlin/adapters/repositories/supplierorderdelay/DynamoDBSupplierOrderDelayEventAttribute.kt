package adapters.repositories.supplierorderdelay

enum class DynamoDBSupplierOrderDelayEventAttribute {

    /**
     * partition key
     */
    PK,

    /**
     * sort key
     */
    SK,

    /**
     * supplier order Id
     */
    OI,

    /**
     * delay
     */
    D,

    /**
     * delay time
     */
    DT;

    val param: String by lazy { this.name }
}

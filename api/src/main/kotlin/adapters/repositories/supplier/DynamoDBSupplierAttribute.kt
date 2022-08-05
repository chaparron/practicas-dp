package adapters.repositories.supplier

enum class DynamoDBSupplierAttribute {

    /**
     * partition key
     */
    PK,

    /**
     * sort key
     */
    SK,

    /**
     * supplier id
     */
    SI,

    /**
     * number
     */
    N,

    /**
     * username
     */
    UN,

    /**
     * code
     */
    C,

    /**
     * state
     */
    S;

    val param: String by lazy { this.name }
}

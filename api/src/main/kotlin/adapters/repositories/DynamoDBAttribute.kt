package adapters.repositories

enum class DynamoDBAttribute {

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
    C;

    val param: String by lazy { this.name }
}
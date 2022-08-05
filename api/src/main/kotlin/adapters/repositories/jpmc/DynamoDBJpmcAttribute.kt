package adapters.repositories.jpmc

enum class DynamoDBJpmcAttribute {

    /**
     * partition key
     */
    PK,

    /**
     * sort key
     */
    SK,

    /**
     * Supplier Order ID
     */
    SOI,

    /**
     * TxnRefNo
     */
    TX,

    /**
     * Amount
     */
    A,

    /**
     * Total Amount
     */
    TA,

    /**
     * Payment Option
     */
    PO,

    /**
     * ResponseCode
     */
    RC,

    /**
     * Message
     */
    M,
    /**
     * EncData
     */
    ED,
    ;

    val param: String by lazy { this.name }
}

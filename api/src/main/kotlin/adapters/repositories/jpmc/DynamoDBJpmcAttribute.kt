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
    /**
     * Create At
     */
    C,
    /**
     * Last Updated At
     */
    LU,
    /**
     * status
     */
    ST
    ;

    val param: String by lazy { this.name }
}

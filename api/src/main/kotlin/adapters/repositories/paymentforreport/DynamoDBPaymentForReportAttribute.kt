package adapters.repositories.paymentforreport

enum class DynamoDBPaymentForReportAttribute {

    // partition key
    PK,
    // sort key
    SK,
    // createdAt
    CA,
    // reportDay
    RD,
    // encData
    ED,
    // supplierOrderId
    SI,
    // amount
    A,
    // paymentOption
    PO,
    // paymentType
    PT,
    // paymentMethod
    PM;
    val param: String by lazy { this.name }

}

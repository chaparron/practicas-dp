package adapters.repositories.user

enum class DynamoDBUserAttribute {
    // partition key
    PK,
    // sort key
    SK,
    // name
    N,
    // mail
    M,
    // country
    C,
    // active
    A,
    // phone
    P,
    // role
    R,
    // created at
    CA,
    // Last Login
    LL,
    // Order
    O;
    val param: String by lazy { this.name }
}

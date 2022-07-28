import domain.model.Supplier
import java.util.UUID

fun randomString() = UUID.randomUUID().toString()

fun anySupplier() = Supplier(
    supplierId = randomString(),
    state = randomString(),
    bankAccountNumber = randomString(),
    indianFinancialSystemCode = randomString()
)

import domain.model.BankAccount
import java.util.UUID

fun randomString() = UUID.randomUUID().toString()

fun anyBankAccount() = BankAccount(
    supplierId = randomString(),
    number = randomString(),
    indianFinancialSystemCode = randomString()
)

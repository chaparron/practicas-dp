package configuration

import adapters.repositories.BankAccountRepository
import adapters.rest.validations.Security
import domain.functions.BankAccountListenerFunction
import domain.services.BankAccountService
import domain.services.ISaleInformationService
import kotlinx.serialization.json.Json

interface Configuration {
    val security: Security
    val jsonMapper: Json
    val saleInformationService: ISaleInformationService
    val bankAccountListenerFunction: BankAccountListenerFunction
}

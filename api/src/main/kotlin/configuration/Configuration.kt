package configuration

import adapters.rest.validations.Security
import domain.services.SaleInformationService
import kotlinx.serialization.json.Json

interface Configuration {
    val security: Security
    val jsonMapper: Json
    val saleInformationService: SaleInformationService
}

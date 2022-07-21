package domain.services

import com.wabi2b.jpmc.sdk.usecase.sale.SaleInformation
import com.wabi2b.jpmc.sdk.usecase.sale.SaleRequest
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import org.slf4j.LoggerFactory
import java.util.*

class SaleInformationService(
    private val saleServiceSdk: SaleService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    //TODO Se debe crear un objeto de dominio propio y no usar el del SDK.
    fun getSaleInformation(amount: String): SaleInformation {
        logger.trace("Starting getSaleInformation")

        //TODO ver como  se meten todas estas properties por variables de entorno / secrets
        val request = SaleRequest(
            version = "1",
            txnRefNo = UUID.randomUUID().toString(),
            amount = amount,
            passCode = "ETKT4295",
            bankId = "001002",
            terminalId = "10010186",
            merchantId = "100000000010588",
            mCC = "5999",
            currency = "356",
            txnType = "Pay",
            returnUrl = "https://webhook.site/537c1b38-f705-4886-8b51-d49ef04b1c76"
        )

        logger.trace("Retrieving sale information from sdk")
        return saleServiceSdk.getSaleInformation(request)
    }

}

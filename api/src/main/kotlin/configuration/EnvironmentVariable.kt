package configuration

enum class EnvironmentVariable {
    JPMC_AES_ENCRYPTION_KEY,
    JPMC_SHAE256_HASH_KEY,
    JPMC_PASS_CODE,
    JPMC_BANK_ID,
    JPMC_TERMINAL_ID,
    JPMC_MERCHANT_ID,
    JPMC_MCC,
    JPMC_CURRENCY,
    JPMC_RETURN_URL_ID;

    companion object {
        fun jpmcConfiguration(): JpmcConfiguration =
            JpmcConfiguration(
                aesEncryptionKey = JPMC_AES_ENCRYPTION_KEY.get(),
                sha256HashKey = JPMC_SHAE256_HASH_KEY.get(),
                passCode = JPMC_PASS_CODE.get(),
                bankId = JPMC_BANK_ID.get(),
                terminalId = JPMC_TERMINAL_ID.get(),
                merchantId = JPMC_MERCHANT_ID.get(),
                mcc = JPMC_MCC.get(),
                currency = JPMC_CURRENCY.get(),
                returnUrl = JPMC_RETURN_URL_ID.get(),
            )
    }

    private val value: String
        get(): String {
            val key = this.name
            return System.getProperty(key, System.getenv(key)).apply {
                if (this.isNullOrBlank()) throw IllegalArgumentException("Missing environment variable: $key")
            }
        }

    private var override: String? = null

    fun get(): String = override ?: value

    fun overrideSetting(value: String) {
        this.override = value
    }

    data class JpmcConfiguration(
        val aesEncryptionKey: String,
        val sha256HashKey: String,
        val passCode: String,
        val bankId: String,
        val terminalId: String,
        val merchantId: String,
        val mcc: String,
        val currency: String,
        val returnUrl: String
    )
}

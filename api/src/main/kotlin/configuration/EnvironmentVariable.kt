package configuration

private const val LIST_DELIMITER = ","

enum class EnvironmentVariable {
    SUPPLIER_TABLE,
    JPMC_AES_ENCRYPTION_KEY,
    JPMC_SHAE256_HASH_KEY,
    JPMC_PASS_CODE,
    JPMC_BANK_ID,
    JPMC_TERMINAL_ID,
    JPMC_MERCHANT_ID,
    JPMC_MCC,
    JPMC_CURRENCY,
    JPMC_RETURN_URL,
    JPMC_VERSION,
    JPMC_AVAILABLE_FOR,
    JPMC_STATE_VALIDATION_ENABLED;

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
                returnUrl = JPMC_RETURN_URL.get(),
                version = JPMC_VERSION.get()
            )

        fun jpmcStateValidationConfig(): JpmcStateValidationConfig =
            JpmcStateValidationConfig(
                availableFor = JPMC_AVAILABLE_FOR.get()
                    .split(LIST_DELIMITER)
                    .filter { it.isNotEmpty() }
                    .map { it.uppercase() },
                enabled = JPMC_STATE_VALIDATION_ENABLED.get().toBoolean()
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
        val returnUrl: String,
        val version: String
    )

    data class JpmcStateValidationConfig(
        val availableFor: List<String>,
        val enabled: Boolean
    )
}

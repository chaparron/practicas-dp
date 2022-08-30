package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentForUpdate(
    val paymentId: Long,
    val paymentOption: String,
    val responseCode: String,
    val message: String,
    val encData: String,
    val status: PaymentStatus,
    val lastUpdatedAt: String)

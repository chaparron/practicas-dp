package domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Customer (
    val name: String,
    val id: String,
    val mail: String,
    val active: Boolean,
    val country: String,
    @Contextual
    val lastLogin: Date,
    val phone: String,
    val role: Role
    )

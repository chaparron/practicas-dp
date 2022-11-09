package domain.model


data class User (
    val name: String,
    val userId: Long,
    val mail: String,
    val country: String,
    val active: Boolean,
    val phone: String,
    val role: Role,
    val createdAt: String,
    val lastLogin: String,
    val orders: List<String>
    )

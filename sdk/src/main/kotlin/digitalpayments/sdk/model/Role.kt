package domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    ADMIN,
    USER,
    SUPPLIER,
    TEST
}

package domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoleResponse {
    ADMIN,
    USER,
    SUPPLIER,
    TEST
}

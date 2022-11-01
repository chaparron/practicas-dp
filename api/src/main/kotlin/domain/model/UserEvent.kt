package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserEvent(
    val userId: Long
) {
    private fun UserEvent(): UserEvent{
        return UserEvent(userId)
    }

    fun doHandle(validator: UserValidator, block: (UserEvent) -> Unit) {
        if(validator.isValid(this))
            block(UserEvent())
    }
}

class UserValidator {

    fun isValid(event: UserEvent): Boolean {
        return true
    }
}

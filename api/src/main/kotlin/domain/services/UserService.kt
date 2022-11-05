package domain.services

import adapters.repositories.user.UserRepository
import org.slf4j.LoggerFactory
import domain.model.User


interface UserService {
    fun save(user: User): User
    fun get(id: Long): User
    fun delete(id: Long)
    fun update(id: Long): User
}

class DefaultUserService(
    private val repository: UserRepository
) : UserService {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultUserService::class.java)
    }

    override fun save(user: User): User {
        logger.info("about to save the user: $user")
        return repository.save(user)
    }

    override fun get(id: Long) = logger.info("about to get the user with id: $id").let {
        repository.get(id)
    }


    override fun delete(id: Long) {
        logger.info("about to delete the user with id: $id")
        repository.delete(id)
    }

    override fun update(id: Long): User {
        TODO()
//        logger.info("about to update the user with id: $id")
//        return repository.update(id)
    }
}

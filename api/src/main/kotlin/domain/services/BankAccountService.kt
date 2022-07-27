package domain.services

import adapters.repositories.BankAccountRepository
import domain.model.BankAccount
import org.slf4j.LoggerFactory

interface BankAccountService {
    fun save(bankAccount: BankAccount): BankAccount
    fun get(supplierId: String): BankAccount
}

class DefaultBankAccountService(
    private val bankAccountRepository: BankAccountRepository
) : BankAccountService {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultBankAccountService::class.java)
    }

    override fun save(bankAccount: BankAccount): BankAccount {
        logger.info("About to save the following bank account $bankAccount")
        return bankAccountRepository.save(bankAccount)
    }

    override fun get(supplierId: String): BankAccount {
        logger.info("About to get bank account for $supplierId")
        return bankAccountRepository.get(supplierId)
    }
}

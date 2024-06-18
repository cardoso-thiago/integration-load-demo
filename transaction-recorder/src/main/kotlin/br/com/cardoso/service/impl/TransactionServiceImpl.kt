package br.com.cardoso.service.impl

import br.com.cardoso.dto.Transaction
import br.com.cardoso.dto.TransactionRevision
import br.com.cardoso.entity.TransactionEntity
import br.com.cardoso.repository.TransactionRepository
import br.com.cardoso.service.TransactionService
import org.springframework.data.history.Revision
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class TransactionServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val transactionTemplate: TransactionTemplate
) : TransactionService {

    override fun saveTransaction(transactionEntity: TransactionEntity): TransactionEntity? {
        return transactionTemplate.execute { transactionRepository.save(transactionEntity) }
    }

    override fun findAllTransactions(): List<Transaction> {
        val allTransactions = transactionRepository.findAll()
        return allTransactions.map { entity ->
            Transaction(
                id = entity.id,
                responseStatus = entity.responseStatus,
                transactionId = entity.transactionId,
                value = entity.transactionValue,
                userName = entity.userName,
                userDocument = entity.userDocument,
                transactionStatus = entity.transactionStatus
            )
        }
    }

    override fun findRevisionsById(id: Long): List<TransactionRevision> {
        val revisions = transactionRepository.findRevisions(id).toList()
        return revisions.map { revision: Revision<Long, TransactionEntity> ->
            TransactionRevision(
                id = revision.entity.id,
                responseStatus = revision.entity.responseStatus,
                transactionId = revision.entity.transactionId,
                value = revision.entity.transactionValue,
                userName = revision.entity.userName,
                userDocument = revision.entity.userDocument,
                transactionStatus = revision.entity.transactionStatus,
                version = revision.entity.version,
                revisionType = revision.metadata.revisionType
            )
        }
    }

    override fun deleteTransactionById(id: Long) {
        transactionTemplate.executeWithoutResult { transactionRepository.deleteById(id) }
    }
}
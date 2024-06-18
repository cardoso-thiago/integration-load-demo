package br.com.cardoso.service

import br.com.cardoso.dto.AuditTransaction
import br.com.cardoso.dto.Transaction
import br.com.cardoso.entity.TransactionEntity

interface TransactionService {

    fun saveTransaction(transactionEntity: TransactionEntity)
    fun findAllTransactions(): List<Transaction>
    fun findRevisionsById(id: Long): List<AuditTransaction>
    fun deleteTransactionById(id: Long)
}
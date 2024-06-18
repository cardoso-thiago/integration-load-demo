package br.com.cardoso.service

import br.com.cardoso.dto.TransactionRevision
import br.com.cardoso.dto.Transaction
import br.com.cardoso.entity.TransactionEntity

interface TransactionService {

    fun saveTransaction(transactionEntity: TransactionEntity): TransactionEntity?
    fun findAllTransactions(): List<Transaction>
    fun findRevisionsById(id: Long): List<TransactionRevision>
    fun deleteTransactionById(id: Long)
}
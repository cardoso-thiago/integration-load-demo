package br.com.cardoso.repository

import br.com.cardoso.entity.TransactionEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.history.RevisionRepository

interface TransactionRepository : CrudRepository<TransactionEntity, Long>,
    RevisionRepository<TransactionEntity, Long, Long>
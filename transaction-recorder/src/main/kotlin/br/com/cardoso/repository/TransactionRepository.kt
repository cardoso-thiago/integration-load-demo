package br.com.cardoso.repository

import br.com.cardoso.entity.TransactionEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : CrudRepository<TransactionEntity, Long>
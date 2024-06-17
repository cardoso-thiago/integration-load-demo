package br.com.cardoso.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
data class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val responseStatus: Int,
    val transactionId: String,
    val value: BigDecimal,
    val userName: String,
    val userDocument: String,
    val userValidation: Int,
    val transactionStatus: String
)
package br.com.cardoso.model

import java.math.BigDecimal

data class TransactionRequest(
    val transactionStatus: String,
    val value: BigDecimal,
    val user: User
)

package br.com.cardoso.model

import java.math.BigDecimal

data class TransactionResponse(
    val transactionStatus: String,
    val value: BigDecimal,
    val user: User,
    val transactionId: String
)


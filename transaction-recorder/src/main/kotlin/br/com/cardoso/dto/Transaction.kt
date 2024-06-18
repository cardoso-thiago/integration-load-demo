package br.com.cardoso.dto

import java.math.BigDecimal

data class Transaction(
    val id: Long,
    val responseStatus: Int?,
    val transactionId: String?,
    val value: BigDecimal?,
    val userName: String?,
    var userDocument: String?,
    val transactionStatus: String?
)
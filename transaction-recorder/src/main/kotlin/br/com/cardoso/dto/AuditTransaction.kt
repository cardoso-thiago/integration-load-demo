package br.com.cardoso.dto

import org.springframework.data.history.RevisionMetadata.RevisionType
import java.math.BigDecimal

data class AuditTransaction(
    val id: Long,
    val responseStatus: Int?,
    val transactionId: String?,
    val value: BigDecimal?,
    val userName: String?,
    var userDocument: String?,
    val transactionStatus: String?,
    val version: Long,
    val revisionType: RevisionType
)
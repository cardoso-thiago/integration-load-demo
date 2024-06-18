package br.com.cardoso.entity

import jakarta.persistence.*
import org.hibernate.envers.Audited
import java.math.BigDecimal

@Table(name = "transaction")
@Entity
@Audited
data class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val responseStatus: Int?,
    val transactionId: String?,
    val value: BigDecimal?,
    val userName: String?,
    var userDocument: String?,
    val transactionStatus: String?,
    @Version
    val version: Long = 0
) {
    @PrePersist
    @PreUpdate
    fun maskFields() {
        userDocument = maskDocument(userDocument)
    }

    private fun maskDocument(document: String?): String? {
        if (document != null) {
            return document.take(3) + "X".repeat(document.length - 3)
        }
        return null
    }
}
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
    @Column(name = "response_status")
    val responseStatus: Int?,
    @Column(name = "transaction_id")
    val transactionId: String?,
    @Column(name = "transaction_value")
    val transactionValue: BigDecimal?,
    @Column(name = "user_name")
    val userName: String?,
    @Column(name = "user_document")
    var userDocument: String?,
    @Column(name = "transaction_status")
    val transactionStatus: String?,
    @Version
    val version: Long = 0
) {
    @PrePersist
    @PreUpdate
    fun maskFields() {
        if (userDocument != null) {
            userDocument = maskDocument(userDocument!!)
        }
    }

    private fun maskDocument(document: String): String {
        return document.take(3) + "X".repeat(document.length - 3)
    }
}
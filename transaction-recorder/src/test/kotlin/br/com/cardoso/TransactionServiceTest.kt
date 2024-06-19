package br.com.cardoso

import br.com.cardoso.configuration.EnversConfiguration
import br.com.cardoso.entity.TransactionEntity
import br.com.cardoso.repository.TransactionRepository
import br.com.cardoso.service.impl.TransactionServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.history.RevisionMetadata
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertNull

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Import(TransactionServiceImpl::class, EnversConfiguration::class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TransactionServiceTest {

    @Autowired
    lateinit var transactionRepository: TransactionRepository
    @Autowired
    lateinit var transactionService: TransactionServiceImpl

    @Test
    fun `should save and find transaction`() {
        //given
        val transactionEntity = getTransactionEntity()
        transactionService.saveTransaction(transactionEntity)
        val transactions = transactionService.findAllTransactions()

        //then
        assertEquals(1, transactions.size)
        assertEquals("123XXXXXX", transactions[0].userDocument)
        assertEquals(transactionEntity.userName, transactions[0].userName)
    }

    @Test
    fun `should find revisions by id`() {
        //given
        val transactionEntity = getTransactionEntity()
        val savedTransaction = transactionService.saveTransaction(transactionEntity)
        transactionService.deleteTransactionById(savedTransaction!!.id)
        val revisions = transactionService.findRevisionsById(savedTransaction.id)

        //then
        assertEquals(2, revisions.size)
        assertEquals("123XXXXXX", revisions[0].userDocument)
        assertEquals(0, BigDecimal(100.0).compareTo(revisions[0].value))
        assertEquals(transactionEntity.userName, revisions[0].userName)
        assertEquals(RevisionMetadata.RevisionType.INSERT, revisions[0].revisionType)
        assertEquals(savedTransaction.id, revisions[1].id)
        assertNull(revisions[1].responseStatus)
        assertNull(revisions[1].transactionId)
        assertNull(revisions[1].value)
        assertNull(revisions[1].userDocument)
        assertNull(revisions[1].transactionStatus)
        assertNull(revisions[1].userName)
        assertEquals(0, revisions[1].version)
        assertEquals(RevisionMetadata.RevisionType.DELETE, revisions[1].revisionType)
    }

    @Test
    fun `should delete transaction by id`() {
        //given
        val transactionEntity = getTransactionEntity()
        val savedTransaction = transactionService.saveTransaction(transactionEntity)
        transactionService.deleteTransactionById(savedTransaction!!.id)
        val transactions = transactionService.findAllTransactions()

        //then
        assertFalse(transactions.any { it.id == savedTransaction.id })
    }

    private fun getTransactionEntity(): TransactionEntity {
        val transactionEntity = TransactionEntity(
            responseStatus = 200,
            transactionId = "123",
            transactionValue = BigDecimal(100.0),
            userName = "John Doe",
            userDocument = "123456789",
            transactionStatus = "AUTHORIZED"
        )
        return transactionEntity
    }
}

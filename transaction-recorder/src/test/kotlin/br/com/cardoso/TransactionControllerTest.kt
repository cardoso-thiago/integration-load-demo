package br.com.cardoso

import br.com.cardoso.controller.TransactionController
import br.com.cardoso.dto.Transaction
import br.com.cardoso.dto.TransactionRevision
import br.com.cardoso.service.TransactionService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.history.RevisionMetadata
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal

@ExtendWith(SpringExtension::class)
@WebMvcTest(TransactionController::class)
class TransactionControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var transactionService: TransactionService

    @Test
    fun `should return all transactions`() {
        //given
        val transactions = getTestTransactions()
        `when`(transactionService.findAllTransactions()).thenReturn(transactions)

        //when and then
        mockMvc.perform(get("/v1/transactions"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(transactions.size))
    }

    @Test
    fun `should return revisions by id`() {
        //given
        val id = 1L
        val revisions = getTestRevisions()
        `when`(transactionService.findRevisionsById(id)).thenReturn(revisions)

        //when and then
        mockMvc.perform(get("/v1/transactions/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(revisions.size))
    }

    @Test
    fun `should delete transaction by id`() {
        //given
        val id = 1L
        Mockito.doNothing().`when`(transactionService).deleteTransactionById(id)

        //when and then
        mockMvc.perform(delete("/v1/transactions/{id}", id)).andExpect(status().isNoContent)
    }

    private fun getTestTransactions(): List<Transaction> {
        val transactions = listOf(
            Transaction(
                id = 1,
                responseStatus = 200,
                transactionId = "123",
                value = BigDecimal(100.00),
                userName = "John Doe",
                userDocument = "1234567890",
                transactionStatus = "AUTHORIZED"
            ),
            Transaction(
                id = 2,
                responseStatus = 200,
                transactionId = "456",
                value = BigDecimal(200.00),
                userName = "John Doe",
                userDocument = "1234567890",
                transactionStatus = "DENIED"
            )
        )
        return transactions
    }

    private fun getTestRevisions(): List<TransactionRevision> {
        val transactionRevisions = listOf(
            TransactionRevision(
                id = 1,
                responseStatus = 200,
                transactionId = "123",
                value = BigDecimal(100.00),
                userName = "John Doe",
                userDocument = "1234567890",
                transactionStatus = "AUTHORIZED",
                version = 0,
                revisionType = RevisionMetadata.RevisionType.INSERT
            ),
            TransactionRevision(
                id = 2,
                responseStatus = 200,
                transactionId = "456",
                value = BigDecimal(200.00),
                userName = "John Doe",
                userDocument = "1234567890",
                transactionStatus = "DENIED",
                version = 0,
                revisionType = RevisionMetadata.RevisionType.INSERT
            )
        )
        return transactionRevisions
    }
}
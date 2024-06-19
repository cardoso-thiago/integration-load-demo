package br.com.cardoso

import br.com.cardoso.dto.Transaction
import br.com.cardoso.dto.TransactionRevision
import br.com.cardoso.listener.KafkaTransactionListener
import br.com.cardoso.model.TransactionRequest
import br.com.cardoso.model.TransactionRequestResponseData
import br.com.cardoso.model.TransactionResponse
import br.com.cardoso.model.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.data.history.RevisionMetadata.RevisionType
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:tc:mysql:8.4.0:///db",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
    ]
)
@EmbeddedKafka(partitions = 1, topics = ["event-hub"])
@ActiveProfiles("test")
class TransactionIntegrationTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper
    @Autowired
    lateinit var kafkaTransactionListener: KafkaTransactionListener
    @Autowired
    lateinit var environment: Environment
    @Autowired
    lateinit var testRestTemplate: TestRestTemplate
    @LocalServerPort
    private val port = 0

    @Test
    fun `should process transaction and validate audit`() {
        //given
        val user = User(name = "John Doe", document = "123456789", validation = 1)
        val (transactionRequest, transactionResponse, transactionRequestResponseData) = generateUserResponseTransactionRequestData(user)
        sendKafkaMessage(transactionRequestResponseData)

        //when
        val allTransactionsResponse: ResponseEntity<List<Transaction>> = testRestTemplate.exchange(
            "http://localhost:$port/v1/transactions",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Transaction>>() {}
        )

        //then
        assertEquals(200, allTransactionsResponse.statusCode.value())
        assertNotNull(allTransactionsResponse.body)
        assertEquals(1, allTransactionsResponse.body!!.size)
        assertEquals(0, transactionResponse.value.compareTo(allTransactionsResponse.body!![0].value))
        assertEquals("123XXXXXX", allTransactionsResponse.body!![0].userDocument)
        assertEquals(transactionResponse.transactionId, allTransactionsResponse.body!![0].transactionId)
        assertEquals(transactionResponse.transactionStatus, allTransactionsResponse.body!![0].transactionStatus)
        assertEquals(user.name, allTransactionsResponse.body!![0].userName)
        assertEquals(user.document, allTransactionsResponse.body!![0].userDocument)
        assertEquals(1, user.validation)
        assertEquals("https://request.uri", transactionRequestResponseData.requestUri)
        assertEquals("POST", transactionRequestResponseData.requestMethod)
        assertEquals(200, transactionRequestResponseData.responseStatus)

        //given
        val savedTransactionId = allTransactionsResponse.body!![0].id

        //when
        val deleteResponse: ResponseEntity<Void> = testRestTemplate.exchange(
            "http://localhost:$port/v1/transactions/$savedTransactionId",
            HttpMethod.DELETE,
            null,
            Void::class.java
        )
        val allTransactionsResponseAfterDelete: ResponseEntity<List<Transaction>> = testRestTemplate.exchange(
            "http://localhost:$port/v1/transactions",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Transaction>>() {}
        )

        //then
        assertEquals(204, deleteResponse.statusCode.value())
        assertTrue(allTransactionsResponseAfterDelete.body!!.isEmpty())

        //when
        val allRevisionsResponseAfterDelete: ResponseEntity<List<TransactionRevision>> = testRestTemplate.exchange(
            "http://localhost:$port/v1/transactions/$savedTransactionId",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<TransactionRevision>>() {}
        )

        //then
        assertEquals(200, allRevisionsResponseAfterDelete.statusCode.value())
        assertNotNull(allRevisionsResponseAfterDelete.body)
        assertEquals(2, allRevisionsResponseAfterDelete.body!!.size)

        assertEquals(0, transactionResponse.value.compareTo(allRevisionsResponseAfterDelete.body!![0].value))
        assertEquals("123XXXXXX", allRevisionsResponseAfterDelete.body!![0].userDocument)
        assertEquals(transactionResponse.transactionId, allRevisionsResponseAfterDelete.body!![0].transactionId)
        assertEquals(transactionResponse.transactionStatus, allRevisionsResponseAfterDelete.body!![0].transactionStatus)
        assertNotEquals(transactionRequest.transactionStatus, allRevisionsResponseAfterDelete.body!![0].transactionStatus)
        assertEquals(user.name, allRevisionsResponseAfterDelete.body!![0].userName)
        assertEquals(0, allRevisionsResponseAfterDelete.body!![0].version)
        assertEquals(RevisionType.INSERT, allRevisionsResponseAfterDelete.body!![0].revisionType)

        assertNull(allRevisionsResponseAfterDelete.body!![1].value)
        assertNull(allRevisionsResponseAfterDelete.body!![1].userDocument)
        assertNull(allRevisionsResponseAfterDelete.body!![1].transactionId)
        assertNull(allRevisionsResponseAfterDelete.body!![1].transactionStatus)
        assertNull(allRevisionsResponseAfterDelete.body!![1].userName)
        assertEquals(0, allRevisionsResponseAfterDelete.body!![1].version)
        assertEquals(RevisionType.DELETE, allRevisionsResponseAfterDelete.body!![1].revisionType)
    }

    private fun sendKafkaMessage(transactionRequestResponseData: TransactionRequestResponseData) {
        //Não enviado via Kafka template para não ter a obrigatoriedade da configuração do producer e aguardar pelo recebimento da mensagem
        val consumerRecordTransaction: ConsumerRecord<String, TransactionRequestResponseData> =
            ConsumerRecord("event-hub", 0, 0, "key", transactionRequestResponseData)
        kafkaTransactionListener.listen(consumerRecordTransaction)
    }

    private fun generateUserResponseTransactionRequestData(user: User): Triple<TransactionRequest, TransactionResponse, TransactionRequestResponseData> {
        val transactionRequest = TransactionRequest(
            transactionStatus = "CREATED",
            value = BigDecimal(1000.00),
            user = user
        )
        val transactionResponse = TransactionResponse(
            transactionId = "123",
            transactionStatus = "AUTHORIZED",
            value = BigDecimal(1000.00),
            user = user
        )
        val transactionRequestResponseData = TransactionRequestResponseData(
            requestUri = "https://request.uri",
            requestMethod = "POST",
            requestBody = objectMapper.writeValueAsString(transactionRequest),
            responseBody = objectMapper.writeValueAsString(transactionResponse),
            responseStatus = 200
        )
        return Triple(transactionRequest, transactionResponse, transactionRequestResponseData)
    }
}
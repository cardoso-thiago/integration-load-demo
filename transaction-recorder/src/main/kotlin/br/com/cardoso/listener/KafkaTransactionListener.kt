package br.com.cardoso.listener

import br.com.cardoso.entity.TransactionEntity
import br.com.cardoso.model.TransactionRequest
import br.com.cardoso.model.TransactionRequestResponseData
import br.com.cardoso.model.TransactionResponse
import br.com.cardoso.service.TransactionService
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaTransactionListener(
    private val objectMapper: ObjectMapper,
    private val transactionService: TransactionService
) {

    @KafkaListener(groupId = "transactionGroup", topics = ["\${kafka.topic}"])
    fun listen(transactionRecord: ConsumerRecord<String, TransactionRequestResponseData>) {
        val transactionRequestResponseData = transactionRecord.value()
        val transactionRequest =
            objectMapper.readValue(transactionRequestResponseData.requestBody, TransactionRequest::class.java)
        val transactionResponse =
            objectMapper.readValue(transactionRequestResponseData.responseBody, TransactionResponse::class.java)

        val transactionEntity = TransactionEntity(
            responseStatus = transactionRequestResponseData.responseStatus,
            transactionId = transactionResponse.transactionId,
            transactionStatus = transactionResponse.transactionStatus,
            transactionValue = transactionRequest.value,
            userDocument = transactionRequest.user.document,
            userName = transactionRequest.user.name,
        )
        transactionService.saveTransaction(transactionEntity)
    }
}
package br.com.cardoso.listener

import br.com.cardoso.model.TransactionRequestResponseData
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaTransactionListener(private val objectMapper: ObjectMapper) {

    @KafkaListener(groupId = "transactionGroup", topics = ["\${kafka.topic}"])
    fun listen(transactionValue: String) {
        val transactionRequestResponseData = objectMapper.readValue(transactionValue, TransactionRequestResponseData::class.java)
        //TODO salva em banco de dados
        println(transactionRequestResponseData)
    }
}
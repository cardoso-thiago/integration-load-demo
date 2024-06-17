package br.com.cardoso.service.impl;

import br.com.cardoso.model.TransactionRequestResponseData;
import br.com.cardoso.service.KafkaMessageService;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class KafkaMessageServiceImpl implements KafkaMessageService {

    private final KafkaTemplate<String, TransactionRequestResponseData> kafkaTemplate;
    private final Environment environment;

    public KafkaMessageServiceImpl(KafkaTemplate<String, TransactionRequestResponseData> kafkaTemplate, Environment environment) {
        this.kafkaTemplate = kafkaTemplate;
        this.environment = environment;
    }

    @Async
    public void sendMessage(TransactionRequestResponseData transactionRequestResponseData) {
        kafkaTemplate.send(Objects.requireNonNull(environment.getProperty("kafka.topic")), transactionRequestResponseData);
    }
}
package br.com.cardoso.service;

import br.com.cardoso.model.TransactionRequestResponseData;

public interface KafkaMessageService {

    void sendMessage(TransactionRequestResponseData transactionRequestResponseData);
}

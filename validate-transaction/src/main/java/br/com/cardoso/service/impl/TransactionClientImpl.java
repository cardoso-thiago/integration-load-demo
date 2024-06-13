package br.com.cardoso.service.impl;

import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.model.CompletedTransaction;
import br.com.cardoso.model.Transaction;
import br.com.cardoso.service.TransactionClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

import java.util.Objects;

public class TransactionClientImpl implements TransactionClient {

    private final RestClient restClient;

    public TransactionClientImpl(RestClient.Builder restClientBuilder, Environment environment) {
        String baseUrl = Objects.requireNonNull(environment.getProperty("transaction.base.url"));
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @WithSpan("validateTransactionRestClient")
    public CompletedTransaction validateTransaction(InitialTransaction initialTransaction) {
        Transaction transaction = new Transaction(initialTransaction.value(), initialTransaction.user());
        return restClient.post().uri("validate/transaction/v1")
                .body(transaction).retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
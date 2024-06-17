package br.com.cardoso.service.impl;

import am.ik.spring.http.client.RetryableClientHttpRequestInterceptor;
import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.exception.TransactionErrorException;
import br.com.cardoso.interceptor.TransactionRequestInterceptor;
import br.com.cardoso.model.CompletedTransaction;
import br.com.cardoso.model.Transaction;
import br.com.cardoso.service.KafkaMessageService;
import br.com.cardoso.service.TransactionClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.Set;

public class TransactionClientImpl implements TransactionClient {

    private final RestClient restClient;

    public TransactionClientImpl(RestClient.Builder restClientBuilder, Environment environment,
                                 KafkaMessageService kafkaMessageService) {
        String baseUrl = Objects.requireNonNull(environment.getProperty("transaction.base.url"));
        this.restClient = restClientBuilder.baseUrl(baseUrl)
                .requestInterceptors(interceptors -> {
                    //Adicionando interceptor para capturar os dados da request e response
                    interceptors.add(new TransactionRequestInterceptor(kafkaMessageService));
                    //Adicionando mecanismo de retry, para que seja feito duas novas tentativas e apenas no caso de statusCode 425
                    //Identificado que deve ser sempre o último, caso contrário, executa novamente em caso de falha sem considerar os demais interceptors.
                    interceptors.add(new RetryableClientHttpRequestInterceptor(new FixedBackOff(100, 2), Set.of(425)));
                })
                .build();
    }

    @WithSpan("validateTransactionRestClient")
    public CompletedTransaction validateTransaction(InitialTransaction initialTransaction) {
        Transaction transaction = new Transaction(initialTransaction.value(), initialTransaction.user());
        return restClient.post().uri("validate/transaction/v1")
                .body(transaction).retrieve()
                .onStatus(status -> status.value() == 425, (request, response) -> {
                    throw new TransactionErrorException(new String(response.getBody().readAllBytes()), response.getStatusCode().value());
                })
                .body(new ParameterizedTypeReference<>() {});
    }
}
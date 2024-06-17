package br.com.cardoso.interceptor;

import br.com.cardoso.model.TransactionRequestResponseData;
import br.com.cardoso.service.KafkaMessageService;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;

public class TransactionRequestInterceptor implements ClientHttpRequestInterceptor {

    private final KafkaMessageService kafkaMessageService;

    public TransactionRequestInterceptor(KafkaMessageService kafkaMessageService) {
        this.kafkaMessageService = kafkaMessageService;
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        try (ClientHttpResponse response = execution.execute(request, body)) {
            TransactionRequestResponseData transactionRequestResponseData = new TransactionRequestResponseData(
                    request.getURI().toString(),
                    request.getMethod().toString(),
                    request.getHeaders(),
                    new String(body),
                    response.getStatusCode().value(),
                    response.getHeaders(),
                    new String(response.getBody().readAllBytes())
            );
            kafkaMessageService.sendMessage(transactionRequestResponseData);
            return response;
        }
    }
}

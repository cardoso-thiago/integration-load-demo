package br.com.cardoso.interceptor;

import br.com.cardoso.model.TransactionRequestResponseData;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;

public class TransactionRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        try (ClientHttpResponse response = execution.execute(request, body)) {
            TransactionRequestResponseData transactionRequestResponseData = new TransactionRequestResponseData(
                    request.getURI().toString(),
                    request.getMethod().toString(),
                    request.getHeaders(),
                    new String(body),
                    response.getStatusCode(),
                    response.getHeaders(),
                    new String(response.getBody().readAllBytes())
            );
            //TODO enviar transactionRequestResponseData como evento
            return response;
        }
    }
}

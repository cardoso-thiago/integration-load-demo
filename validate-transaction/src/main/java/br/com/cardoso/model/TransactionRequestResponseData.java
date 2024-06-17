package br.com.cardoso.model;

import org.springframework.http.HttpHeaders;

public record TransactionRequestResponseData(String requestUri, String requestMethod, HttpHeaders requestHeaders,
                                             String requestBody, int responseStatus, HttpHeaders responseHeaders,
                                             String responseBody) {}

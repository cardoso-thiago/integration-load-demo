package br.com.cardoso.model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

public record TransactionRequestResponseData(String requestUri, String requestMethod, HttpHeaders requestHeaders,
                                             String requestBody, HttpStatusCode responseStatus, HttpHeaders responseHeaders,
                                             String responseBody) {}

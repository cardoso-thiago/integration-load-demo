package br.com.cardoso.model

import org.springframework.http.HttpHeaders

data class TransactionRequestResponseData(
    val requestUri: String,
    val requestMethod: String,
    val requestHeaders: HttpHeaders,
    val requestBody: String,
    val responseStatus: Int,
    val responseHeaders: HttpHeaders,
    val responseBody: String
)
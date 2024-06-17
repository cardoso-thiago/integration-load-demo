package br.com.cardoso.model

import org.springframework.http.HttpHeaders

data class TransactionRequestResponseData(
    val requestUri: String = "",
    val requestMethod: String = "",
    val requestHeaders: HttpHeaders = HttpHeaders(),
    val requestBody: String = "",
    val responseStatus: Int = 0,
    val responseHeaders: HttpHeaders = HttpHeaders(),
    val responseBody: String = ""
)
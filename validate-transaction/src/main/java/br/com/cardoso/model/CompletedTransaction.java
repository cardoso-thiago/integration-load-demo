package br.com.cardoso.model;

import java.math.BigDecimal;

public record CompletedTransaction(String transactionId, BigDecimal value, User user, TransactionStatus transactionStatus) {
}
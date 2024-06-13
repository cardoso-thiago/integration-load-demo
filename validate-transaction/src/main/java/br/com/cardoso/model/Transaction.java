package br.com.cardoso.model;

import java.math.BigDecimal;

public record Transaction(BigDecimal value, User user, TransactionStatus transactionStatus) {

    public Transaction(BigDecimal value, User user) {
        this(value, user, TransactionStatus.CREATED);
    }
}

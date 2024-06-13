package br.com.cardoso.dto;

import br.com.cardoso.model.User;

import java.math.BigDecimal;

public record InitialTransaction(BigDecimal value, User user) {
}

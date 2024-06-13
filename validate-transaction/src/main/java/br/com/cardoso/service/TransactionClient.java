package br.com.cardoso.service;

import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.model.CompletedTransaction;

public interface TransactionClient {

    CompletedTransaction validateTransaction(InitialTransaction initialTransaction);
}

package br.com.cardoso.controller;

import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.model.CompletedTransaction;
import br.com.cardoso.model.TransactionStatus;
import br.com.cardoso.service.TransactionClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction/v1")
public class TransactionController {

    private final TransactionClient transactionClient;

    public TransactionController(TransactionClient transactionClient) {
        this.transactionClient = transactionClient;
    }

    @PostMapping
    public ResponseEntity<TransactionStatus> validateTransaction(@RequestBody InitialTransaction initialTransaction) {
        CompletedTransaction completedTransaction = transactionClient.validateTransaction(initialTransaction);
        return ResponseEntity.ok(completedTransaction.transactionStatus());
    }
}

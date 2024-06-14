package br.com.cardoso.exception;

import java.text.MessageFormat;

public class TransactionErrorException extends RuntimeException {

    public TransactionErrorException(String message, int statusCode) {
        super(MessageFormat.format("Erro ao realizar a transação. Mensagem: {0} => Código de retorno: {1}", message, statusCode));
    }
}

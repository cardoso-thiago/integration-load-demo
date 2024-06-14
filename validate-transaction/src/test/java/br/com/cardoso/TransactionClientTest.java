package br.com.cardoso;

import br.com.cardoso.configuration.ClientConfiguration;
import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.exception.TransactionErrorException;
import br.com.cardoso.model.CompletedTransaction;
import br.com.cardoso.model.TransactionStatus;
import br.com.cardoso.model.User;
import br.com.cardoso.service.TransactionClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
@Import(ClientConfiguration.class)
public class TransactionClientTest {

    @Autowired
    MockRestServiceServer mockRestServiceServer;
    @Autowired
    TransactionClient transactionClient;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    Environment environment;

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void shouldReturnTransactionStatusWhenTransactionIsSent(TransactionStatus expectedStatus) throws JsonProcessingException {
        //given
        String baseUrl = Objects.requireNonNull(environment.getProperty("transaction.base.url"));
        BigDecimal value = new BigDecimal(1000);
        User user = new User("John Doe", "123456789", 0);
        InitialTransaction initialTransaction = new InitialTransaction(value, user);
        CompletedTransaction completedTransaction = new CompletedTransaction(UUID.randomUUID().toString(), value, user, expectedStatus);

        //when
        mockRestServiceServer.expect(requestTo(baseUrl + "/validate/transaction/v1"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(completedTransaction), MediaType.APPLICATION_JSON));

        //then
        CompletedTransaction completedTransactionValidate = transactionClient.validateTransaction(initialTransaction);

        assertEquals(completedTransaction, completedTransactionValidate);
    }

    @Test
    void shouldThrownTransactionErrorExceptionWhenTransactionIsSentAndApiIsDown() {
        //given
        String baseUrl = Objects.requireNonNull(environment.getProperty("transaction.base.url"));
        BigDecimal value = new BigDecimal(1000);
        User user = new User("John Doe", "123456789", 0);
        InitialTransaction initialTransaction = new InitialTransaction(value, user);
        CompletedTransaction completedTransaction = new CompletedTransaction(UUID.randomUUID().toString(), value, user, TransactionStatus.ERROR);
        String apiErrorMessage = "Falha temporária, tente novamente em alguns instantes.";
        int statusCode = 425;
        String expectedErrorMessage = MessageFormat.format("Erro ao realizar a transação. Mensagem: {0} => Código de retorno: {1}",
                apiErrorMessage, statusCode);

        //when
        mockRestServiceServer.expect(times(3), requestTo(baseUrl + "/validate/transaction/v1"))
                .andRespond(withStatus(HttpStatusCode.valueOf(statusCode))
                        .body(apiErrorMessage));

        //then
        TransactionErrorException transactionErrorException =
                assertThrows(TransactionErrorException.class, () -> transactionClient.validateTransaction(initialTransaction));
        assertEquals(expectedErrorMessage, transactionErrorException.getMessage());
    }
}

package br.com.cardoso;

import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.exception.TransactionErrorException;
import br.com.cardoso.model.CompletedTransaction;
import br.com.cardoso.model.TransactionStatus;
import br.com.cardoso.model.User;
import br.com.cardoso.service.KafkaMessageService;
import br.com.cardoso.service.TransactionClient;
import br.com.cardoso.service.impl.TransactionClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.MockServerRestClientCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

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
public class TransactionClientTest {

    @Autowired
    MockServerRestClientCustomizer mockServerRestClientCustomizer;
    @Autowired
    RestClient.Builder restClientBuilder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    Environment environment;
    @MockBean
    KafkaMessageService kafkaMessageService;
    TransactionClient transactionClient;
    MockRestServiceServer mockRestServiceServer;

    @BeforeEach
    void setup() {
        // Alteração necessária no teste ainda em linha com a issue https://github.com/spring-projects/spring-boot/issues/38832,
        //foi necessário passar a utilizar o MockServerRestClientCustomizer para chamar o setBufferContent para simular o comportamento
        //do BufferingClientHttpRequestFactory, necessário para o novo interceptor que captura as informações de request e response,
        //permitindo a leitura do response mais de uma vez. Como não é possível configurar o RestClient.Builder devido a issue, foi
        //necessário configurar o builder em tempo de teste que usa o MockMvc para simular a requisição. Alteração realizada com base na doc:
        //https://docs.spring.io/spring-boot/api/java/org/springframework/boot/test/web/client/MockServerRestClientCustomizer.html
        mockServerRestClientCustomizer.setBufferContent(true);
        mockServerRestClientCustomizer.customize(restClientBuilder);
        transactionClient = new TransactionClientImpl(restClientBuilder, environment, kafkaMessageService);
        mockRestServiceServer = mockServerRestClientCustomizer.getServer(restClientBuilder);
    }

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void shouldReturnTransactionStatusWhenTransactionIsSent(TransactionStatus expectedStatus) throws JsonProcessingException {
        //given
        String baseUrl = Objects.requireNonNull(environment.getProperty("transaction.base.url"));
        BigDecimal value = new BigDecimal(1000);
        User user = new User("John Doe", "123456789", 0);
        InitialTransaction initialTransaction = new InitialTransaction(value, user);
        CompletedTransaction completedTransaction = new CompletedTransaction(UUID.randomUUID().toString(), value, user, expectedStatus);
        mockRestServiceServer.expect(requestTo(baseUrl + "/validate/transaction/v1"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(completedTransaction), MediaType.APPLICATION_JSON));

        //when
        CompletedTransaction completedTransactionValidate = transactionClient.validateTransaction(initialTransaction);

        //then
        assertEquals(completedTransaction, completedTransactionValidate);
    }

    @Test
    void shouldThrownTransactionErrorExceptionWhenTransactionIsSentAndApiIsDown() {
        //given
        String baseUrl = Objects.requireNonNull(environment.getProperty("transaction.base.url"));
        BigDecimal value = new BigDecimal(1000);
        User user = new User("John Doe", "123456789", 0);
        InitialTransaction initialTransaction = new InitialTransaction(value, user);
        String apiErrorMessage = "Falha temporária, tente novamente em alguns instantes.";
        int statusCode = 425;
        String expectedErrorMessage = MessageFormat.format("Erro ao realizar a transação. Mensagem: {0} => Código de retorno: {1}",
                apiErrorMessage, statusCode);
        mockRestServiceServer.expect(times(3), requestTo(baseUrl + "/validate/transaction/v1"))
                .andRespond(withStatus(HttpStatusCode.valueOf(statusCode))
                        .body(apiErrorMessage));

        //when and then
        TransactionErrorException transactionErrorException =
                assertThrows(TransactionErrorException.class, () -> transactionClient.validateTransaction(initialTransaction));
        assertEquals(expectedErrorMessage, transactionErrorException.getMessage());
    }
}

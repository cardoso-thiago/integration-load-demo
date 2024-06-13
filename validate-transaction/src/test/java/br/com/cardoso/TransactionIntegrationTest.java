package br.com.cardoso;

import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.model.CompletedTransaction;
import br.com.cardoso.model.TransactionStatus;
import br.com.cardoso.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {0})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @LocalServerPort
    private int port;

    @BeforeAll
    static void setup(MockServerClient mockServerClient) {
        String baseUrl = "http://localhost:" + mockServerClient.getPort();
        System.setProperty("transaction.base.url", baseUrl);

        mockServerClient.when(request().withMethod("POST").withPath("/validate/transaction/v1"))
                .respond(HttpTemplate.template(HttpTemplate.TemplateType.VELOCITY,
                        "#set($jsonBody = $json.parse($!request.body)){'statusCode': 200,'body': " +
                                "{'transactionId': '$!uuid', 'value': $jsonBody.value, " +
                                "'user': {'fullname': '$jsonBody.user.fullName', 'document': '$jsonBody.user.document', 'validation': $jsonBody.user.validation}, " +
                                "'transactionStatus': #if($jsonBody.user.validation == -1) 'ERROR' #elseif($jsonBody.user.validation == 0) 'DENIED' #else 'AUTHORIZED' #end}}"));

    }

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void whenSentTransactionShouldReturnTransactionStatus(TransactionStatus expectedStatus) throws JsonProcessingException {
        //given
        BigDecimal value = new BigDecimal(1000);
        int validation = switch (expectedStatus) {
            case DENIED -> 0;
            case AUTHORIZED -> 1;
            default -> -1;
        };
        User user = new User("John Doe", "123456789", validation);
        InitialTransaction initialTransaction = new InitialTransaction(value, user);
        CompletedTransaction completedTransaction = new CompletedTransaction(UUID.randomUUID().toString(), value, user, expectedStatus);

        String requestBody = objectMapper.writeValueAsString(initialTransaction);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        //then
        ResponseEntity<TransactionStatus> response = testRestTemplate.postForEntity("http://localhost:" + port + "/transaction/v1", entity, TransactionStatus.class);

        //No cenário integrado, a transação nunca deve retornar com o status CREATED
        if (expectedStatus.equals(TransactionStatus.CREATED)) {
            assertEquals(TransactionStatus.ERROR, response.getBody());
        } else {
            assertEquals(expectedStatus, response.getBody());
        }
    }
}
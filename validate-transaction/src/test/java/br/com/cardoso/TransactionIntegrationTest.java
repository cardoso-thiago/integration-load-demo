package br.com.cardoso;

import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.model.TransactionRequestResponseData;
import br.com.cardoso.model.TransactionStatus;
import br.com.cardoso.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {0})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"event-hub"})
public class TransactionIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @LocalServerPort
    private int port;
    
    private static final List<TransactionRequestResponseData> transactionRequestResponseDataList = new ArrayList<>();

    @BeforeAll
    static void setup(MockServerClient mockServerClient) {
        String baseUrl = "http://localhost:" + mockServerClient.getPort();
        System.setProperty("transaction.base.url", baseUrl);

        mockServerClient.when(request().withMethod("POST").withPath("/validate/transaction/v1"), Times.exactly(2))
                .respond(HttpTemplate.template(HttpTemplate.TemplateType.VELOCITY,
                        "{'statusCode': 425,'body': 'Falha temporária, tente novamente em alguns instantes.'}"));

        mockServerClient.when(request().withMethod("POST").withPath("/validate/transaction/v1"))
                .respond(HttpTemplate.template(HttpTemplate.TemplateType.VELOCITY,
                        "#set($jsonBody = $json.parse($!request.body)){'statusCode': 200,'body': " +
                                "{'transactionId': '$!uuid', 'value': $jsonBody.value, " +
                                "'user': {'fullname': '$jsonBody.user.fullName', 'document': '$jsonBody.user.document', 'validation': $jsonBody.user.validation}, " +
                                "'transactionStatus': #if($jsonBody.user.validation == -1) 'ERROR' #elseif($jsonBody.user.validation == 0) 'DENIED' #else 'AUTHORIZED' #end}}"));
    }

    @AfterAll
    static void shouldValidateAllTransactionStatusWhenListReceivedKafkaMessages() {
        //given
        await().atMost(10, SECONDS).until(() -> transactionRequestResponseDataList.size() == 4);
        List<String> responseList = transactionRequestResponseDataList.stream().map(TransactionRequestResponseData::responseBody).toList();

        //then
        assertTrue(responseList.stream().anyMatch(response -> response.contains(TransactionStatus.ERROR.name())),
                "Esperado que ao menos uma resposta tenha o status de 'ERROR'");
        assertTrue(responseList.stream().anyMatch(response -> response.contains(TransactionStatus.AUTHORIZED.name())),
                "Esperado que ao menos uma resposta tenha o status de 'AUTHORIZED'");
        assertTrue(responseList.stream().anyMatch(response -> response.contains(TransactionStatus.DENIED.name())),
                "Esperado que ao menos uma resposta tenha o status de 'DENIED'");
        assertFalse(responseList.stream().anyMatch(response -> response.contains(TransactionStatus.CREATED.name())),
                "Nenhuma resposta deve ter o status 'CREATED'");
    }

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void shouldReturnTransactionStatusWhenTransactionIsSent(TransactionStatus expectedStatus) throws JsonProcessingException {
        //given
        BigDecimal value = new BigDecimal(1000);
        int validation = switch (expectedStatus) {
            case DENIED -> 0;
            case AUTHORIZED -> 1;
            default -> -1;
        };

        User user = new User("John Doe", "123456789", validation);
        InitialTransaction initialTransaction = new InitialTransaction(value, user);

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

    @KafkaListener(groupId = "transactionGroup", topics = "event-hub")
    public void listen(ConsumerRecord<String, String> record) throws JsonProcessingException {
        transactionRequestResponseDataList.add(objectMapper.readValue(record.value(), TransactionRequestResponseData.class));
    }
}
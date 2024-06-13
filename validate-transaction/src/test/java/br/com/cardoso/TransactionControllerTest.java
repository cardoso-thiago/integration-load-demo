package br.com.cardoso;

import br.com.cardoso.controller.TransactionController;
import br.com.cardoso.dto.InitialTransaction;
import br.com.cardoso.model.CompletedTransaction;
import br.com.cardoso.model.TransactionStatus;
import br.com.cardoso.model.User;
import br.com.cardoso.service.TransactionClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TransactionClient transactionClient;
    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void whenSentTransactionShouldReturnTransactionStatus(TransactionStatus expectedStatus) throws Exception {
        //given
        BigDecimal value = new BigDecimal(1000);
        User user = new User("John Doe", "123456789", 0);
        InitialTransaction initialTransaction = new InitialTransaction(value, user);
        CompletedTransaction completedTransaction = new CompletedTransaction(UUID.randomUUID().toString(), value, user, expectedStatus);

        //when
        when(transactionClient.validateTransaction(initialTransaction)).thenReturn(completedTransaction);

        //then
        this.mockMvc.perform(post("/transaction/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialTransaction)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("\"" + expectedStatus.toString() + "\""));
    }
}

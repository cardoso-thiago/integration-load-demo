package br.com.cardoso.configuration;

import br.com.cardoso.service.TransactionClient;
import br.com.cardoso.service.impl.TransactionClientImpl;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    @Bean
    TransactionClient transactionClient(RestClient.Builder restClientBuilder, Environment environment) {
        restClientBuilder.requestFactory(new BufferingClientHttpRequestFactory(new JdkClientHttpRequestFactory()));
        return new TransactionClientImpl(restClientBuilder, environment);
    }

    /**
     * Customização para mudança do request factory sem quebra dos testes unitários com o uso da anotação @RestClientTest.
     * Workaround identificado na issue https://github.com/spring-projects/spring-boot/issues/38832. Existe também a issue
     * https://github.com/spring-projects/spring-boot/issues/36266 para habilitar uma maneira simplificada de configurar
     * o request factory de maneira global.
     */
    @Bean
    RestClientCustomizer restClientCustomizer() {
        //JdkClientHttpRequestFactory
        //JettyClientHttpRequestFactory
        //SimpleClientHttpRequestFactory
        //Adicionando o BufferingClientHttpRequestFactory. Pode causar um aumento no consumo de memória e latência,
        //principalmente em cenários com respostas grandes
        return (restClientBuilder) -> restClientBuilder.requestFactory(new BufferingClientHttpRequestFactory(new JdkClientHttpRequestFactory()));
    }
}

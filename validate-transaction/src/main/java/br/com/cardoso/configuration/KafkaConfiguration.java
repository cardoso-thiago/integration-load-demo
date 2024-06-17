package br.com.cardoso.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Objects;

@Configuration
public class KafkaConfiguration {

    //Cria o tópico se ele não existir
    @Bean
    public NewTopic topic(Environment environment) {
        return TopicBuilder.name(Objects.requireNonNull(environment.getProperty("kafka.topic")))
                .partitions(10).replicas(1).build();
    }
}

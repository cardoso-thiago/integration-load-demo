package br.com.cardoso.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfiguration {

    //Cria o tópico se ele não existir
    @Bean
    fun topic(environment: Environment) = TopicBuilder.name(environment.getProperty("kafka.topic")!!)
        .partitions(1).replicas(1).build()
}
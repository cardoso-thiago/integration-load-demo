# Projeto Integrado

O objetivo desse projeto é avaliar as integrações entre peças considerando o melhor tempo de execução de acordo com um caso de uso fictício. A ideia geral vai em linha com avaliar mecanismos e integrações utilizando os dados da execução de testes de carga, com tracing (e eventualmente com métricas) para identificar gargalos e possíveis opções de melhorias.

O projeto consiste atualmente nos seguintes módulos:

- `load-test`: Realiza os testes de carga. Mais detalhes sobre os processos para execução e resultados já coletados no README do módulo.
- `validate-transaction`: Recebe uma solicitação de `transação` e encaminha para a API que valida se a transação foi realizada com sucesso. A validação da transação foi feita com o `MockServer`. De maneira assíncrona utilizando mecanismos do `Spring Boot`, encaminha os dados de request e response para um tópico do `Kafka`. Mais detalhes sobre a execução e escolhas de implementação no README do módulo.
- `transaction-recorder`: Recebe a mensagem publicada no tópico encaminhada pela aplicação `validate-transaction` e salva no banco de dados. Utiliza o `spring-data-envers` para gerar dados de auditoria. Mais detalhes sobre a execução e escolhas de implementação no README do módulo.
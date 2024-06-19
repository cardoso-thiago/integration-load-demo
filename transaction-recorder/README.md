# Grava Transação

## Regras

- Deve receber os dados de request e response das transações
- Os dados devem ser salvos em uma banco de dados, com ofuscação de dados sensíveis, para ser utilizado posteriormente como fonte de auditoria

## Execução da aplicação

Subir a aplicação `transaction-recorder`. Com a aplicação de pé, chamadas para o `validate-transaction` devem gerar o recebimento de uma mensagem com os dados de request e response da transação.

- Para validar as transações inseridas, basta executar o comando: `curl localhost:8081/v1/transactions`
- Para consultar as informações de auditoria de uma transação, basta executar o comando: `curl localhost:8081/v1/transactions/<id>`, onde o `id` corresponde a uma das transações da consulta anterior.
- Para verificar o mecanismo de auditoria completamente, primeiramente pode ser executado o comando para deleção de uma transação com o comando: `curl -X DELETE localhost:8081/v1/transactions/<id>`. Novamente, o `id` corresponde a uma das transações da primeira consulta.
  - Agora pode ser executado novamente o comando para consulta de auditoria: `curl localhost:8081/v1/transactions/<id>`. Dessa vez, o id deve ser o mesmo da deleção anterior.

## Execução dos testes de mutação

Na raiz do projeto, basta executar o comando `gradle pitest`. Ao fim do processo, o relatório pode ser visualizado em `build/reports/pitest`.

### Motivação para algumas escolhas

- Implementação do mecanismo de auditoria utilizando o `spring-data-envers` que utiliza o `hibernate-envers` por debaixo dos panos. Esse mecanismo facilita o processo de auditoria, criando uma tabela auxiliar (`entidade_AUD`) que armazena as interações que ocorrem ao longo do tempo em um determinado registro.
- Não estava previsto no escopo inicial um processo de deleção ou edição da transação, porém para validar o mecanismo de auditoria, foi adicionado um endpoint para deleção de uma transação.
- Devido também ao processo de auditoria, foi necessário adicionar o operador `?` nos atributos da entidade e nos DTOs, pois nos casos de deleção, os valores retornam nulos, marcando como operação de deleção na coluna `revisionType` da tabela de auditoria
- Devido ao retorno de response com dados duplicados para experimentação com o `MockServer`, foram necessárias algumas validações que em um cenário padrão seriam desnecessárias, para atingir uma coberturaa aceitável nos testes de mutação. 
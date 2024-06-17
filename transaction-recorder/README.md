# Grava Transação

## Regras

- Deve receber os dados de request e response das transações
- Os dados devem ser salvos em uma banco de dados, com ofuscação de dados sensíveis, para ser utilizado posteriormente como fonte de auditoria

## Execução da aplicação

Subir a aplicação `transaction-recorder`. Com a aplicação de pé, chamadas para o `validate-transaction` devem gerar o recebimento de uma mensagem com os dados de request e response da transação.

## Execução dos testes de mutação

EM CONSTRUÇÃO

### Motivação para algumas escolhas

- O parse do tipo de objeto não foi transparente. Para um melhor tratamento do payload, nesse ponto o valor é enviado como _Json_ mas recebido como _String_. O parse do objeto é feito localmente, com auxílio do `ObjectMapper`.
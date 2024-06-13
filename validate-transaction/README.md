# Valida Transação

## Preparo inicial

- Criação da network: `docker network create demo-load`
- Na raiz do projeto completo, subir o ambiente: `docker-compose-up`

## Regras

- Deve receber uma transação que será validada ou negada
- Os dados de envio da transação e recebimento devem ser encaminhados ao fim do processo para o serviço processamento da transação
- O retorno deve ser o mais rápido possível

## Execução da aplicação

Subir a aplicação `validate-transaction`. Com a aplicação de pé, pode ser realizado o seguinte curl para validação de uma nova transação:

```shell
curl -X POST http://localhost:8080/transaction/v1 \
-H "Content-Type: application/json" \
-d '{ "value": 100.0, "user": { "fullName": "John Doe", "document": "123456789", "validation": 2 }}' 
```

O campo `validation` é o que determina o resultado de acordo com o mock no `mockserver`. Se o valor do campo for `-1`, o resultado da transação será "ERROR", se for `0`, o resultado será `DENIED`. Outros valores darão o retorno `AUTHORIZED`.

## Execução dos testes de carga

Acessando o projeto `load-test` no terminal, basta executar o comando: `mvn gatling:test -Dgatling.simulationClass=transaction.HeavyLoadValidateTransaction`

## Execução dos testes de mutação

Na raiz do projeto, basta executar o comando `gradle pitest`. Ao fim do processo, o relatório pode ser visualizado em `build/reports/pitest`
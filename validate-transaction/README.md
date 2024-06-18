# Valida Transação

## Preparo inicial

- Criação da network: `docker network create demo-load`
- Na raiz do projeto completo, subir o ambiente: `docker-compose-up`

## Regras

- Deve receber uma transação que será validada ou negada
- Os dados de envio da transação e recebimento devem ser encaminhados ao fim do processo para o serviço gravação da transação
- O retorno deve ser o mais rápido possível
- Deve publicar as informações em um hub de eventos que recebe as informações via mensageria

## Execução da aplicação

Subir a aplicação `validate-transaction`. Com a aplicação de pé, pode ser realizado o seguinte curl para validação de uma nova transação:

```shell
curl -X POST http://localhost:8080/v1/validate/transactions \
-H "Content-Type: application/json" \
-d '{ "value": 100.0, "user": { "name": "John Doe", "document": "123456789", "validation": 2 }}' 
```

O campo `validation` é o que determina o resultado de acordo com o mock no `mockserver`. Se o valor do campo for `-1`, o resultado da transação será `ERROR`, se for `0`, o resultado será `DENIED`. Outros valores darão o retorno `AUTHORIZED`.

## Execução dos testes de carga

Acessando o projeto `load-test` no terminal, basta executar o comando: `mvn gatling:test -Dgatling.simulationClass=transaction.HeavyLoadValidateTransaction`

## Execução dos testes de mutação

Na raiz do projeto, basta executar o comando `gradle pitest`. Ao fim do processo, o relatório pode ser visualizado em `build/reports/pitest`

### Motivação para algumas escolhas

- Utilização do `JdkClientHttpRequestFactory`: Foi realizado um benchmark com testes de cargas para validar a `RequestFactory` com a melhor performance. Foram testados `JdkClientHttpRequestFactory`, `JettyClientHttpRequestFactory` e `SimpleClientHttpRequestFactory`. Houve basicamente um empate entre o `Jetty` e o `Jdk`, a opção final pelo `Jdk` foi principalmente devido ao fato de não precisar de uma dependência adicional.
- Uso do `MockServer` para simulação da API de validação da transação: Apesar do WireMock ser mais conhecido, o uso do `RestClient` com `HTTP2` no momento é problemático. O `MockServer` parece não estar sendo atualizado, mas funcionou perfeitamente e fornece opções interessantes de configuração e template de resposta.
- Uso do mecanismo de Async do Spring: Implementação muito simples, trouxe um ganho considerável na resposta, identificado inicialmente por logs com o tempo de execução sem a implementação e com a implementação. Foi possível identificar o fluxo sendo executado em uma nova thread com a informação em `Thread.currentThread().getName()`. É possível que em um cenário de Virtual Threads e alta carga, o benefício possa ser ainda maior.
- O payload de response faz pouco sentido em um cenário real. Foi implementado dessa forma mais como forma de aprendizado dos mecanismos de template do `MockServer`.

## Erro na execução com cobertura pelo IntelliJ

Ao executar os testes com cobertura pelo IntelliJ, os testes de integração podem falhar. Esse problema foi mapeado [aqui](https://youtrack.jetbrains.com/issue/IDEA-274803/Velocity-field-names-check-fails-with-new-coverage).
No caso, é necessário realizar a seguinte configuração na IDE: 

- Menu `Navigate`
- Menu `Search Everywhere` e digite `Registry...`
- Buscar pela chave `idea.coverage.new` e desabilitar. Os testes podem ficar mais lentos com essas opções desabilitadas. 